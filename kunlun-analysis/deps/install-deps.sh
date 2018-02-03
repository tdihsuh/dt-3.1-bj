#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# Spark core
# Version: 1.1.0 
mvn install:install-file -Dfile=spark-core_2.10-1.1.0.jar      -DgroupId=org.apache.spark     -DartifactId=spark-core_2.10        -Dversion=1.1.0   -Dpackaging=jar

#Spark mllib
# Version: 1.1.0 
mvn install:install-file -Dfile=spark-mllib_2.10-1.1.0.jar -DgroupId=org.apache.spark -DartifactId=spark-mllib_2.10   -Dversion=1.1.0   -Dpackaging=jar

#elasticsearch hdoop-spark_2.10
# Version: 2.1.0.Beta3
mvn install:install-file -Dfile=elasticsearch-spark_2.10-2.1.0.Beta3.jar -DgroupId=org.elasticsearch -DartifactId=elasticsearch-spark_2.10   -Dversion=2.1.0.Beta3   -Dpackaging=jar
