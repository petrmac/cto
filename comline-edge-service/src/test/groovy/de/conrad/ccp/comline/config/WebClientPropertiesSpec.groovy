package de.conrad.ccp.comline.config

import spock.lang.Specification

/**
 * Test specification for WebClientProperties configuration class.
 */
class WebClientPropertiesSpec extends Specification {

    def "should have correct default values"() {
        given: "a new WebClientProperties instance"
        def properties = new WebClientProperties()

        expect: "default values are set correctly"
        properties.connectionTimeout == 10000
        properties.readTimeout == 30000
        properties.writeTimeout == 10000
        properties.maxConnections == 100
        properties.pendingAcquireTimeout == 45000
        properties.useJvmDnsResolver == false
    }

    def "should have correct nested property default values"() {
        given: "a new WebClientProperties instance"
        def properties = new WebClientProperties()

        expect: "nested properties have correct defaults"
        properties.connection.timeout == 10000
        properties.read.timeout == 30000
        properties.write.timeout == 10000
        properties.max.connections == 100
        properties.pending.acquire.timeout == 45000
    }

    def "should allow setting connection timeout"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting connection timeout"
        properties.connectionTimeout = 5000

        then: "the value is updated"
        properties.connectionTimeout == 5000
    }

    def "should allow setting read timeout"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting read timeout"
        properties.readTimeout = 60000

        then: "the value is updated"
        properties.readTimeout == 60000
    }

    def "should allow setting write timeout"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting write timeout"
        properties.writeTimeout = 15000

        then: "the value is updated"
        properties.writeTimeout == 15000
    }

    def "should allow setting max connections"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting max connections"
        properties.maxConnections = 200

        then: "the value is updated"
        properties.maxConnections == 200
    }

    def "should allow setting pending acquire timeout"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting pending acquire timeout"
        properties.pendingAcquireTimeout = 60000

        then: "the value is updated"
        properties.pendingAcquireTimeout == 60000
    }

    def "should allow enabling JVM DNS resolver"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "enabling JVM DNS resolver"
        properties.useJvmDnsResolver = true

        then: "the value is updated"
        properties.useJvmDnsResolver == true
    }

    def "should allow setting nested connection timeout"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting nested connection timeout"
        properties.connection.timeout = 8000

        then: "the value is updated"
        properties.connection.timeout == 8000
    }

    def "should allow setting nested read timeout"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting nested read timeout"
        properties.read.timeout = 45000

        then: "the value is updated"
        properties.read.timeout == 45000
    }

    def "should allow setting nested write timeout"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting nested write timeout"
        properties.write.timeout = 12000

        then: "the value is updated"
        properties.write.timeout == 12000
    }

    def "should allow setting nested max connections"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting nested max connections"
        properties.max.connections = 150

        then: "the value is updated"
        properties.max.connections == 150
    }

    def "should allow setting nested pending acquire timeout"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting nested pending acquire timeout"
        properties.pending.acquire.timeout = 50000

        then: "the value is updated"
        properties.pending.acquire.timeout == 50000
    }

    def "should initialize nested objects automatically"() {
        given: "a new WebClientProperties instance"
        def properties = new WebClientProperties()

        expect: "nested objects are not null"
        properties.connection != null
        properties.read != null
        properties.write != null
        properties.max != null
        properties.pending != null
        properties.pending.acquire != null
    }

    def "should support builder-style configuration"() {
        given: "a WebClientProperties instance"
        def properties = new WebClientProperties()

        when: "setting multiple properties in builder style"
        properties.with {
            connectionTimeout = 15000
            readTimeout = 45000
            useJvmDnsResolver = true
        }

        then: "all values are updated"
        properties.connectionTimeout == 15000
        properties.readTimeout == 45000
        properties.useJvmDnsResolver == true
    }

    def "nested Connection class should have correct defaults"() {
        given: "a new Connection instance"
        def connection = new WebClientProperties.Connection()

        expect: "default timeout is set"
        connection.timeout == 10000
    }

    def "nested Read class should have correct defaults"() {
        given: "a new Read instance"
        def read = new WebClientProperties.Read()

        expect: "default timeout is set"
        read.timeout == 30000
    }

    def "nested Write class should have correct defaults"() {
        given: "a new Write instance"
        def write = new WebClientProperties.Write()

        expect: "default timeout is set"
        write.timeout == 10000
    }

    def "nested Max class should have correct defaults"() {
        given: "a new Max instance"
        def max = new WebClientProperties.Max()

        expect: "default connections is set"
        max.connections == 100
    }

    def "nested Pending.Acquire class should have correct defaults"() {
        given: "a new Pending.Acquire instance"
        def acquire = new WebClientProperties.Pending.Acquire()

        expect: "default timeout is set"
        acquire.timeout == 45000
    }
}
