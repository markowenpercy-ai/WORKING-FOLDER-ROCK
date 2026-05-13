package com.go2super.packet;

import com.go2super.buffer.Go2Buffer;
import com.go2super.database.entity.User;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartListener;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.service.JobService;
import com.go2super.service.LoginService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.Socket;
import java.util.*;

public class PacketRouter {

    private static PacketRouter instance;

    private final LinkedList<SmartListener> smartListeners = new LinkedList<>();
    private final Map<Integer, Class<? extends Packet>> packetsMap = new HashMap<>();

    public PacketRouter() {

        instance = this;
    }

    public boolean containsPacket(int packetId) {

        return packetsMap.containsKey(packetId);
    }

    public void broadcast(Packet packet, User... excludes) {

        List<LoggedGameUser> gameUsers = LoginService.getInstance().getGameUsers();

        main:
        for (LoggedGameUser user : gameUsers) {
            for (User exclude : excludes) {
                if (exclude.getGuid() == user.getGuid()) {
                    continue main;
                }
            }
            user.getSmartServer().send(packet);

        }

    }

    @SneakyThrows
    public void fireEvent(Packet packet) {

        for (SmartListener listener : smartListeners) {
            if (packet.getClass().isAssignableFrom(listener.getPacketClass())) {
                if (!Arrays.asList(502, 503, 1017).contains(packet.getType())) {
                    BotLogger.packet("🗲 INVOKE " + listener.getPacketMethod().getDeclaringClass().getName() + "#" + listener.getPacketMethod().getName());
                }

                Method packetMethod = listener.getPacketMethod();
                packetMethod.setAccessible(true);

                packetMethod.invoke(listener.getInstance(), packet);

            }
        }

    }

    @SneakyThrows
    public void craftPackets() {

        // Use ClasspathHelper to properly locate package in Spring Boot fat JAR
        ClassLoader cl = getClass().getClassLoader();
        
        ConfigurationBuilder config = new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage("com.go2super.packet", cl))
            .setExpandSuperTypes(true);

        Reflections reflections = new Reflections(config);
        Set<Class<? extends Packet>> classes = reflections.getSubTypesOf(Packet.class);

        for (Class<? extends Packet> classPacket : classes) {

            Field[] fields = FieldUtils.getAllFields(classPacket);

            int type = -1;

            for (Field field : fields) {
                if (field.getName().equals("TYPE")) {
                    type = field.getInt(classPacket.newInstance());
                }
            }
            if (type >= 0) {
                packetsMap.put(type, classPacket);
            }
        }
    }

    @SneakyThrows
    public void craftListeners() {

        // Use ClasspathHelper to properly locate package in Spring Boot fat JAR
        ClassLoader cl = getClass().getClassLoader();
        
        ConfigurationBuilder config = new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage("com.go2super.listener", cl))
            .setExpandSuperTypes(true);

        Reflections reflections = new Reflections(config);
        Set<Class<? extends PacketListener>> classes = reflections.getSubTypesOf(PacketListener.class);

        for (Class<? extends PacketListener> classListener : classes) {
            for (Method declaredMethod : classListener.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(PacketProcessor.class)) {
                    for (Parameter parameter : declaredMethod.getParameters()) {
                        if (parameter.getType().getSuperclass() != null && parameter.getType().getSuperclass().isAssignableFrom(Packet.class)) {
                            PacketListener packetListener = BeanUtils.instantiateClass(classListener);
                            PacketProcessor packetProcessor = declaredMethod.getAnnotation(PacketProcessor.class);
                            Class<? extends Packet> packetClass = (Class<? extends Packet>) parameter.getType();
                            smartListeners.add(SmartListener.of(packetClass, packetProcessor, declaredMethod, packetListener));
                        }
                    }
                }
            }
        }

    }

    @SneakyThrows
    public void playPacket(int size, int type, Go2Buffer buffer, Socket socket, SmartServer smartServer) {

        if (!packetsMap.containsKey(type)) {
            BotLogger.packet("⚠ NOT FOUND " + type);
            return;
        }
        Class<? extends Packet> packetClass = packetsMap.get(type);
        Packet packet = packetClass.newInstance();
        try {
            String guid = null;

            Class<?> objectClass = packet.getClass();
            for (Field field : objectClass.getDeclaredFields()) {
                if (field.getName().equals("guid")) {
                    field.setAccessible(true);
                    guid = String.valueOf(field.get(packet));
                }
            }

            fireEvent(packet.map(size, type, buffer, socket, smartServer));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public boolean contains(Method method, Method... interfaceMethods) {

        for (Method cache : interfaceMethods) {
            if (cache.getName().equals(method.getName())) {
                return true;
            }

        }

        return false;

    }

    public static PacketRouter getInstance() {

        return instance;
    }

}
