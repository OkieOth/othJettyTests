apply plugin: 'java'
//apply plugin: 'application'
apply plugin: 'groovy'

def projectMainClass='de.othsoft.examples.jetty'
def projectTitle='This is a sample title - change'

sourceCompatibility = '1.7'
[compileJava, compileTestJava]*.options*.encoding = 'UTF-8'

project.group = 'de.othsoft.examples.jetty'
project.version = '0.1-SNAPSHOT'

if (!hasProperty('mainClass')) {
    ext.mainClass = projectMainClass
}


uploadArchives {
    repositories {
        flatDir {
                dirs "${System.getenv('HOME')}/myGradleRepos/${project.group}/${project.name}"
        }
    }
}

repositories {
    ivy {
        url "file://${System.getenv('HOME')}/myGradleRepos"
        layout "pattern", {
            artifact "[organisation]/[artifact]/[artifact]-[revision].[ext]"
        }
    }
    mavenCentral()
}

dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.10'
    compile 'org.slf4j:slf4j-api:1.7.5'
    compile 'ch.qos.logback:logback-classic:1.0.9'
    compile 'org.eclipse.jetty:jetty-server:9.3.4.v20151007'
    compile 'commons-cli:commons-cli:1.3'
    testCompile 'org.codehaus.groovy:groovy-all:2.4.3'
}

configurations {
    integrationTestCompile.extendsFrom testCompile
    integrationTestRuntime.extendsFrom testRuntime
}

sourceSets {
    integrationTest {
        java {
            compileClasspath += main.output + test.output
            runtimeClasspath += main.output + test.output
            srcDir file('src/integration_test/java')
        }
        groovy {
            compileClasspath += main.output + test.output
            runtimeClasspath += main.output + test.output
            srcDir file('src/integration_test/groovy')
        }
        /* not needed this time
        resources.srcDir file('src/integration-test/resources')
        */
    }
}

task integrationTest(type: Test) {
    doFirst {
        println 'before integration tests - ' + new Date().getTime()
/*
        println 'create fat jar'
        fatJar
        println 'copy fat jar to vagrant dir'
        copy {
            from 'build/libs'
            into 'src/integration_test/vagrant/ubuntu_logserver/tmp'
            include 'simpleServer-all-0.1-SNAPSHOT.jar'
        }
        // that's the place to start the test vagrant machine        
        println 'start vm'
        "${project.rootDir}/src/integration_test/scripts/startVagrant.sh".execute().in.eachLine {
            println it
        }
        println 'vm started'
        println 'retrieve the ip address of the vm'
        "${project.rootDir}/src/integration_test/scripts/print_vm_address.sh".execute().in.eachLine {
            println it
       }
*/
    }
    
    doLast {
/*
        // that's the place to start the test vagrant machine
        "${project.rootDir}/src/integration_test/scripts/stopVagrant.sh".execute().in.eachLine {
            println it
        }
*/
        println 'after integration tests - ' + new Date().getTime()
    }
    testClassesDir = sourceSets.integrationTest.output.classesDir
    classpath = sourceSets.integrationTest.runtimeClasspath
    outputs.upToDateWhen { false }
}

check.dependsOn integrationTest
integrationTest.mustRunAfter test


tasks.withType(Test) {
    reports.html.destination = file("${reporting.baseDir}/${name}")
}

task fatJar(type: Jar) {
        manifest {
        attributes 'Implementation-Title': projectTitle,  
                'Implementation-Version': version,
                'Main-Class': projectMainClass
    }
    baseName = project.name + '-all'
    from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}

