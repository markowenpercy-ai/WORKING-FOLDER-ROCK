import http.server
import urllib.request
import urllib.error
import json
import os
import re
import subprocess
import sys

BACKEND = "http://localhost:9090"
CLIENT_DIR = "/home/Delk/Desktop/WORKING FOLDER ROCK/client"
LOG_DIR = "/home/Delk/Desktop/WORKING FOLDER ROCK/logs"
CMD_EXEC = "/home/Delk/Desktop/WORKING FOLDER ROCK/cmd-exec.py"

class ProxyHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=CLIENT_DIR, **kwargs)

    def do_GET(self):
        if self.path.startswith("/api/"):
            self.proxy_api("GET")
        elif self.path.startswith("/backend/"):
            self.proxy_backend("GET")
        elif self.path.startswith("/logs/"):
            fname = self.path[len("/logs/"):]
            fpath = os.path.normpath(os.path.join(LOG_DIR, fname))
            if fpath.startswith(LOG_DIR) and os.path.isfile(fpath):
                with open(fpath, "r", errors="replace") as f:
                    data = f.read()
                self.send_response(200)
                self.send_header("Content-Type", "text/plain; charset=utf-8")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(data.encode("utf-8"))
            else:
                self.send_error(404, "Log not found")
        else:
            if self.path == "/":
                self.path = "/index.html"
            fpath = self.translate_path(self.path)
            if os.path.isfile(fpath):
                super().do_GET()
            else:
                self.send_error(404, "Not found")

    def do_POST(self):
        if self.path.startswith("/api/"):
            self.proxy_api("POST")
        elif self.path.startswith("/backend/"):
            self.proxy_backend("POST")
        else:
            self.send_error(404, "Not found")

    def read_body(self):
        length = int(self.headers.get("Content-Length", 0))
        return json.loads(self.rfile.read(length)) if length else {}

    def req(self, method, path, body=None, headers=None):
        url = BACKEND + path
        data = json.dumps(body).encode() if body else None
        req = urllib.request.Request(url, data=data, method=method)
        req.add_header("Content-Type", "application/json")
        if headers:
            for k, v in headers.items():
                req.add_header(k, v)
        try:
            with urllib.request.urlopen(req) as resp:
                return resp.status, json.loads(resp.read())
        except urllib.error.HTTPError as e:
            return e.code, json.loads(e.read())

    def send_json(self, status, data):
        body = json.dumps(data).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def proxy_api(self, method):
        path = self.path[4:]
        body = self.read_body() if method == "POST" else None
        auth = {"Authorization": self.headers["Authorization"]} if "Authorization" in self.headers else {}

        try:
            if path == "/login":
                code, res = self.req("POST", "/login/login/account", body)
                if res.get("code") == 200:
                    self.send_json(200, {"success": True, "token": res["data"]["token"]})
                else:
                    msg = res.get("message", "ACCOUNT_NOT_FOUND") if isinstance(res, dict) else "ACCOUNT_NOT_FOUND"
                    self.send_json(401, {"error": msg})

            elif path == "/register":
                code, res = self.req("POST", "/login/register/account", body)
                if res.get("code") == 200:
                    self.send_json(200, {"success": True})
                else:
                    msg = res.get("message", "REGISTRATION_FAILED") if isinstance(res, dict) else "REGISTRATION_FAILED"
                    self.send_json(400, {"error": msg})

            elif path == "/play-user":
                uid = body.get("userId", "0")
                code, res = self.req("GET", f"/account/play/user/{uid}", headers=auth)
                if res.get("code") == 200:
                    d = res["data"]
                    self.send_json(200, {"success": True, "userId": d["userId"], "sessionKey": d["sessionKey"]})
                else:
                    self.send_json(401, {"error": "UNAUTHORIZED"})

            elif path == "/logout":
                code, res = self.req("GET", "/account/logout/user", headers=auth)
                self.send_json(200, {"success": True})

            elif path == "/cmd/execute":
                if not body or not body.get("auth_token") or not body.get("command"):
                    self.send_json(400, {"error": "auth_token and command required"})
                    return
                uid = body.get("user_id", 0)
                guid = body.get("guid", 1)
                result = subprocess.run(
                    [sys.executable, CMD_EXEC, body["auth_token"], str(uid), str(guid), body["command"]],
                    capture_output=True, text=True, timeout=30
                )
                if result.returncode == 0:
                    try:
                        data = json.loads(result.stdout.strip())
                        self.send_json(200, data)
                    except:
                        self.send_json(200, {"output": result.stdout.strip()})
                else:
                    self.send_json(500, {"error": result.stderr.strip() or "Command execution failed"})

            elif path == "/status":
                code, on = self.req("GET", "/metrics/online")
                c, patch = self.req("GET", "/metrics/patch")
                self.send_json(200, {
                    "authenticated": False,
                    "cdnUrl": "http://localhost:8080",
                    "gameIp": "localhost",
                    "onlinePlayers": on.get("data", {}).get("online", 0) if isinstance(on, dict) else 0,
                    "username": None,
                    "users": []
                })

            elif path == "/accounts/list/user":
                code, res = self.req("GET", "/account/list/user", headers=auth)
                if res.get("code") == 200:
                    self.send_json(200, {"code": 200, "data": res.get("data", [])})
                else:
                    self.send_json(200, {"code": 200, "data": []})

            elif path == "/accounts/create/user":
                code, res = self.req("POST", "/account/create/user", body, headers=auth)
                if res.get("code") == 200:
                    users = res.get("data", res.get("users", []))
                    self.send_json(200, {"success": True, "users": users})
                else:
                    self.send_json(400, {"error": res.get("message", "FAILED")})

            else:
                self.send_json(404, {"error": "NOT_FOUND"})

        except Exception as e:
            self.send_json(500, {"error": str(e)})

    def proxy_backend(self, method):
        path = '/' + self.path[len("/backend/"):]  # strip /backend/ prefix, restore leading /
        body = self.read_body() if method == "POST" else None
        headers = {}
        for k in ("Authorization", "Content-Type"):
            if k in self.headers:
                headers[k] = self.headers[k]
        code, data = self.req(method, path, body, headers)
        self.send_json(code, data)

    def log_message(self, fmt, *args):
        if len(args) >= 3:
            print(f"[{args[0]}] {args[1]} -> {args[2]}")
        else:
            print(fmt % args)

if __name__ == "__main__":
    port = 8080
    http.server.HTTPServer(("0.0.0.0", port), ProxyHandler).serve_forever()
