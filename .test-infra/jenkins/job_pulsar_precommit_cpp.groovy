/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import common_job_properties

// This is the Java precommit which runs a maven install, and the current set of precommit tests.
freeStyleJob('pulsar_precommit_cpp') {
    description('precommit CPP/Python client tests verification for pull requests of <a href="http://pulsar.apache.org">Apache Pulsar</a>.')

    // Execute concurrent builds if necessary.
    concurrentBuild()

    // Set common parameters.
    common_job_properties.setTopLevelMainJobProperties(delegate)

    // Sets that this is a PreCommit job.
    common_job_properties.setPreCommit(delegate, 'C++ / Python Tests')

    steps {
        // Build Java Code
        maven {
            // Set Maven parameters.
            common_job_properties.setMavenConfig(delegate)

            goals('-B clean package')
            properties(skipTests: true, interactiveMode: false)
        }

        shell('''
        echo "Build C++ client library"
        export CMAKE_ARGS="-DCMAKE_BUILD_TYPE=Debug -DBUILD_DYNAMIC_LIB=OFF"

        pulsar-client-cpp/docker-build.sh
        pulsar-client-cpp/docker-tests.sh
        ''')
    }

    publishers {
        archiveArtifacts {
            allowEmpty(true)
            pattern('**/target/container-logs/**')
             // archiveJunit doesn't capture everything, so copy these files
            pattern('**/surefire-reports/TEST-*.xml')
            pattern('**/surefire-reports/*.txt')
            // pre and post scripts should output to .debug-info files if needed
            pattern('*.debug-info')
        }
        archiveJunit('**/surefire-reports/TEST-*.xml') {
            allowEmptyResults(true)
        }
    }
}
