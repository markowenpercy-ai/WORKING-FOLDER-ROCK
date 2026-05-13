package com.go2super.service.jobs;

import com.go2super.obj.entry.SmartServer;
import com.go2super.packet.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class UserTask extends Task {

    private SmartServer smartServer;
    private Packet request;

}
