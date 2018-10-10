package net.minecraftforge.gradle.common.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraftforge.gradle.common.util.Utils;

public class MCPConfigV1 extends Config {
    public static MCPConfigV1 get(InputStream stream) {
        return Utils.fromJson(stream, MCPConfigV1.class);
    }
    public static MCPConfigV1 get(byte[] data) {
        return get(new ByteArrayInputStream(data));
    }



    private String version; // Minecraft version
    private Map<String, Object> data;
    private Map<String, List<Step>> steps;
    private Map<String, Function> functions;
    private Map<String, List<String>> libraries;

    public String getVersion() {
        return version;
    }

    public Map<String, Object> getData() {
        return data == null ? Collections.emptyMap() : data;
    }

    @SuppressWarnings("unchecked")
    public String getData(String... path) {
        if (data == null)
            return null;
        Map<String, Object> level = data;
        for (String part : path) {
            if (!level.containsKey(part))
                return null;
            Object val = level.get(part);
            if (val instanceof String)
                return (String)val;
            if (val instanceof Map)
                level = (Map<String, Object>)val;
        }
        return null;
    }

    public List<Step> getSteps(String side) {
        List<Step> ret = steps == null ? null : steps.get(side);
        return ret == null ? Collections.emptyList() : ret;
    }

    public Function getFunction(String name) {
        return functions == null ? null : functions.get(name);
    }

    public Map<String, Function> getFunctions() {
        return functions == null ? Collections.emptyMap() : functions;
    }

    public List<String> getLibraries(String side) {
        List<String> ret = libraries == null ? null : libraries.get(side);
        return ret == null ? Collections.emptyList() : ret;
    }

    public static class Step {
        private final String type;
        private final String name;
        private final Map<String, String> values;

        private Step(String type, String name, Map<String, String> values) {
            this.type = type;
            this.name = name;
            this.values = values;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public Map<String, String> getValues() {
            return values == null ? Collections.emptyMap() : values;
        }

        public String getValue(String key) {
            return values == null ? null : values.get(key);
        }

        public static class Deserializer implements JsonDeserializer<Step> {
            @Override
            public Step deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject obj = json.getAsJsonObject();
                if (!obj.has("type"))
                    throw new JsonParseException("Could not parse step: Missing 'type'");
                String type = obj.get("type").getAsString();
                String name = obj.has("name") ? obj.get("name").getAsString() : type;
                Map<String, String> values = obj.entrySet().stream()
                        .filter(e -> !"type".equals(e.getKey()) && !"name".equals(e.getKey()))
                        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getAsString()));
                return new Step(type, name, values);
            }
        }
    }

    public static class Function {
        private String version; //Maven artifact for the jar to run
        private String repo; //Maven repo to download the jar from
        private List<String> args;
        private List<String> jvmargs;

        public String getVersion() {
            return version;
        }

        public String getRepo() {
            return repo == null ? "https://libraries.minecraft.net/" : repo;
        }

        public List<String> getArgs() {
            return args == null ? Collections.emptyList() : args;
        }

        public List<String> getJvmArgs() {
            return jvmargs == null ? Collections.emptyList() : jvmargs;
        }
    }
}