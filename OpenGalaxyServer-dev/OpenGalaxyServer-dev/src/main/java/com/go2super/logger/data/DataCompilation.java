package com.go2super.logger.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Setter
@Getter
@ToString
public class DataCompilation {

    private Map<String, Object> compilation = new HashMap<>();

    public void add(String key, Object value) {

        compilation.put(key, value);
    }

}
