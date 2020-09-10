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
- user to check
- repo to check
- sprint list

please use java 11 or higher

## Execute
put the jar and the settings.yaml on the same directory then run the command below
```
java -jar Kontribute.main.jar
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
