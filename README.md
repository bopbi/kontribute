Kontribute
=========
Tool to measure developer contribution to a Github Repo, this tool will generate an XLS that will contain the PR information, with its commit, url, contributor issue, sprint duration, and its weight

the tool uses the github graphql v4 API

Features:
- generate XLS for user contribution
- link the used contribution to a issue

## Setup
you need a personal access token, please configure it by following https://developer.github.com/v4/guides/forming-calls/#authenticating-with-graphql
get the jar and the settings.yaml from the release
modify the settings.yml to include the information below:
- token
- users list to check
- repos list to check
- sprint date range list

note: 
please use java 11 or higher
make sure you put the jar and the settings.yaml on the same directory 

## Generate Contribution List
the command below will generate the contribution list
```
java -jar Kontribute.main.jar
```

## Generate PR Reviewer List
the command below will generate the contribution list
```
java -jar Kontribute.main.jar r
```

## Generate PR Reviewer and Contribution List
the command below will generate the pr review and the contribution list
```
java -jar Kontribute.main.jar r k
```

License
--------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
