package com.ofg.infrastructure.discovery.watcher.presence

import com.ofg.infrastructure.discovery.watcher.presence.checker.NoInstancesRunningException
import com.ofg.infrastructure.discovery.watcher.presence.checker.PresenceChecker
import org.apache.curator.x.discovery.ServiceCache
import org.apache.curator.x.discovery.ServiceInstance
import spock.lang.Specification

import static org.codehaus.groovy.runtime.StackTraceUtils.extractRootCause

class DefaultDependencyPresenceOnStartupVerifierSpec extends Specification {

    private static final String SERVICE_NAME = 'service01'

    def 'should throw exception if obligatory dependencies are missing'() {
        given:
            DefaultDependencyPresenceOnStartupVerifier dependencyVerifier = new DefaultDependencyPresenceOnStartupVerifier()
            ServiceCache serviceCache = Mock()
            serviceCache.instances >> new ArrayList<ServiceInstance>()
        when:
            dependencyVerifier.verifyDependencyPresence(SERVICE_NAME, serviceCache, true)
        then:
            Throwable thrown = thrown(Throwable)
            extractRootCause(thrown).class == NoInstancesRunningException
    }

    def 'should check optional dependency using optional dependency checker'() {
        given:
            PresenceChecker optionalDependencyChecker = Mock()
            DependencyPresenceOnStartupVerifier dependencyVerifier = new DependencyPresenceOnStartupVerifier(optionalDependencyChecker) {
            }
            ServiceCache serviceCache = Mock()
            serviceCache.instances >> new ArrayList<ServiceInstance>()
        when:
            dependencyVerifier.verifyDependencyPresence(SERVICE_NAME, serviceCache, false)
        then:
            1 * optionalDependencyChecker.checkPresence(SERVICE_NAME, serviceCache.instances)
    }
    
}
