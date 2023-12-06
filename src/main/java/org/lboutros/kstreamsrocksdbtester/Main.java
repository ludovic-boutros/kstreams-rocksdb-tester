package org.lboutros.kstreamsrocksdbtester;

import lombok.extern.slf4j.Slf4j;

import javax.naming.ConfigurationException;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try (RocksDBTest rocksDBTest = new RocksDBTest()) {
            rocksDBTest.run();
        } catch (ConfigurationException | org.apache.commons.configuration2.ex.ConfigurationException e) {
            log.error("Error during test: ", e);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
