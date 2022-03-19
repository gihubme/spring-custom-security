package eu.nnn4.springjwt2022.config;

import org.springframework.core.env.Environment;

public class PropertyresolutionHelper {

    public static Object getProperty(String name, Environment env) {

        if (System.getenv(name)!=null && !System.getenv(name).isBlank())
            return System.getenv(name);

        if (System.getenv(name)!=null && !System.getProperty(name).isBlank())
            return System.getProperty(name);

        if (env.getProperty(name)!=null && !env.getProperty(name).isBlank())
            return env.getProperty(name);

        else throw new IllegalArgumentException("Property with name: "+name+" wasn't found");
    }
}
