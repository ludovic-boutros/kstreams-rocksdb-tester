package org.lboutros.kstreamsrocksdbtester.configuration;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;

import javax.naming.ConfigurationException;
import java.util.Properties;

public class Utils {

    public static Properties readConfiguration(String propertyFileName) throws ConfigurationException, org.apache.commons.configuration2.ex.ConfigurationException {
        return readConfiguration(null, propertyFileName);
    }

    public static Properties readConfiguration(String systemPropertyFileName, String propertyFileName) throws ConfigurationException, org.apache.commons.configuration2.ex.ConfigurationException {
        if (systemPropertyFileName != null) {
            Configuration systemConfiguration = ((PropertiesConfiguration) new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                    .configure(new Parameters()
                            .properties()
                            .setFileName(systemPropertyFileName))
                    .getConfiguration())
                    .interpolatedConfiguration();

            Properties systemProperties = ConfigurationConverter.getProperties(systemConfiguration);
            systemProperties.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
        }
        Configuration configuration = ((PropertiesConfiguration) new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(new Parameters()
                        .properties()
                        .setFileName(propertyFileName))
                .getConfiguration())
                .interpolatedConfiguration();

        return ConfigurationConverter.getProperties(configuration);
    }

}
