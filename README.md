# Quarkus Artemis
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-11-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.artemis/quarkus-artemis-jms?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.artemis/quarkus-artemis-jms)

## Introduction

This Quarkus extension enables the use of the Apache ActiveMQ Artemis JMS client in Quarkus.

It provides configuration properties to configure the JMS client and native executable support.

It is a replacement for the `quarkus-artemis-jms` extension originally part of the Quarkus core distribution.

Next to the JMS client, it also enables the use of the Apache ActiveMQ Artemis Core client (used by the JMS client).
It is automatically enabled if `quarkus-artemis-core` is added as dependency without `quarkus-artemis-jms`.

## Documentation

The documentation for this extension can be found [here](https://docs.quarkiverse.io/quarkus-artemis/dev/index.html).

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/middagj"><img src="https://avatars.githubusercontent.com/u/157566?v=4?s=100" width="100px;" alt="Jacob Middag"/><br /><sub><b>Jacob Middag</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/commits?author=middagj" title="Code">💻</a> <a href="#maintenance-middagj" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/turing85"><img src="https://avatars.githubusercontent.com/u/32584495?v=4?s=100" width="100px;" alt="Marco Bungart"/><br /><sub><b>Marco Bungart</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/commits?author=turing85" title="Code">💻</a> <a href="#maintenance-turing85" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://zhfeng.github.io/"><img src="https://avatars.githubusercontent.com/u/1246139?v=4?s=100" width="100px;" alt="Zheng Feng"/><br /><sub><b>Zheng Feng</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/commits?author=zhfeng" title="Code">💻</a> <a href="#maintenance-zhfeng" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/louisa-fr"><img src="https://avatars.githubusercontent.com/u/126324666?v=4?s=100" width="100px;" alt="louisa-fr"/><br /><sub><b>louisa-fr</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/commits?author=louisa-fr" title="Code">💻</a> <a href="#maintenance-louisa-fr" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://fxnn.de/"><img src="https://avatars.githubusercontent.com/u/6599417?v=4?s=100" width="100px;" alt="Felix Neumann"/><br /><sub><b>Felix Neumann</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/commits?author=fxnn" title="Documentation">📖</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://www.linkedin.com/in/dreampearl/"><img src="https://avatars.githubusercontent.com/u/16265285?v=4?s=100" width="100px;" alt="Rakhi Kumari"/><br /><sub><b>Rakhi Kumari</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/commits?author=DreamPearl" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/gtudan"><img src="https://avatars.githubusercontent.com/u/419425?v=4?s=100" width="100px;" alt="Gregor Tudan"/><br /><sub><b>Gregor Tudan</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/commits?author=gtudan" title="Code">💻</a></td>
    </tr>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="http://gastaldi.wordpress.com"><img src="https://avatars.githubusercontent.com/u/54133?v=4?s=100" width="100px;" alt="George Gastaldi"/><br /><sub><b>George Gastaldi</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/commits?author=gastaldi" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/vsevel"><img src="https://avatars.githubusercontent.com/u/6041620?v=4?s=100" width="100px;" alt="Vincent Sevel"/><br /><sub><b>Vincent Sevel</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/issues?q=author%3Avsevel" title="Bug reports">🐛</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/fmjaeschke"><img src="https://avatars.githubusercontent.com/u/15345365?v=4?s=100" width="100px;" alt="Frank-Michael Jaeschke"/><br /><sub><b>Frank-Michael Jaeschke</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-artemis/issues?q=author%3Afmjaeschke" title="Bug reports">🐛</a> <a href="https://github.com/quarkiverse/quarkus-artemis/commits?author=fmjaeschke" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/timonzi"><img src="https://avatars.githubusercontent.com/u/47322311?v=4?s=100" width="100px;" alt="timonzi"/><br /><sub><b>timonzi</b></sub></a><br /><a href="#ideas-timonzi" title="Ideas, Planning, & Feedback">🤔</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
