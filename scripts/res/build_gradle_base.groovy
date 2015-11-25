apply plugin: 'java'
//apply plugin: 'application'
apply plugin: 'groovy'
apply plugin: 'maven-publish'

def projectMainClass='de.othsoft.examples.jetty'
def projectTitle='This is a sample title - change'
def vagrantMachineName='ubuntu_logserver'

sourceCompatibility = '1.7'
[compileJava, compileTestJava]*.options*.encoding = 'UTF-8'

project.group = 'de.othsoft.examples.jetty'
project.version = '0.1-SNAPSHOT'

if (!hasProperty('mainClass')) {
    ext.mainClass = projectMainClass
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
        }
    }
}

publishing {
    repositories {
        maven {
            url "${System.getenv('HOME')}/myMavenRepos"
        }
    }
}

repositories {
    maven {
        url "${System.getenv('HOME')}/myMavenRepos"
    }
    mavenCentral()
}

dependencies {
    compile 'de.othsoft.helper:othJettyHelper:0.1' 
    testCompile group: 'junit', name: 'junit', version: '4.10'
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
        println 'start vm'
        "${project.rootDir}/src/integration_test/scripts/startVagrant.sh".execute().in.eachLine {
            println it
        }
        println 'vm started'
        println 'retrieve the ip address of the vm'
        "${project.rootDir}/src/integration_test/scripts/print_vm_address.sh".execute().in.eachLine {
            println it
       }
    }
    
    doLast {
        "${project.rootDir}/src/integration_test/scripts/stopVagrant.sh".execute().in.eachLine {
            println it
        }
        println 'after integration tests - ' + new Date().getTime()
    }
    testClassesDir = sourceSets.integrationTest.output.classesDir
    classpath = sourceSets.integrationTest.runtimeClasspath
    outputs.upToDateWhen { false }
}

tasks.withType(Test) {
    reports.html.destination = file("${reporting.baseDir}/${name}")
}

task copyFatJar(type: Copy) {
    def fatJarName = "${project.name}-all-${project.version}.jar"
    outputs.files file("${project.rootDir}/build/libs/$fatJarName")
    println "copy fatJar '${fatJarName}' from '${project.rootDir}/build/libs' to '${project.rootDir}/src/integration_test/vagrant/${vagrantMachineName}/tmp'"
    copy {
        from "${project.rootDir}/build/libs"
        into "${project.rootDir}/src/integration_test/vagrant/${vagrantMachineName}/tmp"
        include fatJarName
    }    
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

copyFatJar.dependsOn fatJar
copyFatJar.mustRunAfter testClasses
integrationTest.dependsOn copyFatJar

check.dependsOn integrationTest
integrationTest.mustRunAfter test

