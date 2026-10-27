# beBoard
beBoard is a privacy-conscious and customizable open-source keyboard for Android.
It does not use the internet permission, and thus is 100% offline.

beBoard is built on top of [HeliBoard](https://github.com/HeliBorg/HeliBoard), and keeps up to date with it. All the features of HeliBoard are available, plus the beBoard additions listed below.

## Table of Contents

- [What's new in beBoard](#whats-new-in-beboard)
- [Features](#features)
- [Download](#download)
- [Contributing](#contributing)
   * [Reporting Issues](#reporting-issues)
   * [Code Contribution](CONTRIBUTING.md)
- [Links](#links)
- [License](#license)
- [Credits](#credits)

# What's new in beBoard
* **Adaptive key touch areas**: letters that are likely to come next get a slightly larger invisible touch area, similar to the iOS keyboard. The keys look exactly the same, but taps close to the edge between two letters are more likely to land on the letter you meant.
  * likelihoods come from the keyboard's own suggestions and next word predictions, so it works with any language that has a dictionary
  * only letters are affected, never space, shift, delete, numbers, symbols or long-press popups
  * a key can only grow up to a quarter of its size, so taps near the middle of a key always hit that key
  * can be turned off in _Settings → Text correction → Adaptive key touch areas_

# Features
<ul>
  <li>Add dictionaries for suggestions and spell check</li>
  <ul>
    <li>build your own, or get them  <a href="https://codeberg.org/Helium314/aosp-dictionaries#dictionaries">here</a> (quality may vary)</li>
    <li>additional dictionaries for emojis or scientific symbols can be used to provide suggestions (similar to "emoji search")</li>
    <li>note that for Korean layouts, suggestions only work using <a href="https://github.com/openboard-team/openboard/commit/83fca9533c03b9fecc009fc632577226bbd6301f">this dictionary</a>, the tools in the dictionary repository are not able to create working dictionaries</li>
  </ul>
  <li>Adaptive key touch areas (see above)</li>
  <li>Customize keyboard themes (style, colors and background image)</li>
  <ul>
    <li>can follow the system's day/night setting on Android 10+ (and on some versions of Android 9)</li>
    <li>can follow dynamic colors for Android 12+</li>
  </ul>
  <li>Emoji search (inline and separate, requires <a href="https://codeberg.org/Helium314/aosp-dictionaries">emoji dictionary</a>)</li>
  <li>Customize keyboard <a href="layouts.md">layouts</a> (only available when disabling <i>use system languages</i>)</li>
  <li>Customize special layouts, like symbols, number,  or functional key layout</li>
  <li>Multilingual typing</li>
  <li>Glide typing (<i>only with closed source library</i> ☹️)</li>
  <ul>
    <li>library not included in the app, as there is no compatible open source library available</li>
    <li>can be extracted from GApps packages ("<i>swypelibs</i>"), or downloaded <a href="https://github.com/erkserkserks/openboard/tree/46fdf2b550035ca69299ce312fa158e7ade36967/app/src/main/jniLibs">here</a> (click on the file and then "raw" or the tiny download button)</li>
    <li>load it in <i>Settings → Advanced → Load gesture typing library</i></li>
  </ul>
  <li>Clipboard history</li>
  <li>One-handed mode</li>
  <li>Split keyboard</li>
  <li>Number pad</li>
  <li>Backup and restore your settings and learned word / history data</li>
</ul>

Most features work the same as in HeliBoard, so for the [FAQ](https://github.com/HeliBorg/HeliBoard/wiki/FAQ), [hidden features](https://github.com/HeliBorg/HeliBoard/wiki/9.-Hidden-features) and more information, the [HeliBoard wiki](https://github.com/HeliBorg/HeliBoard/wiki) is the best place to look.

# Download
APKs are published on the [releases page](../../releases) of this repository.

You can also build a debug APK yourself by running the _Build debug APK_ workflow in the _Actions_ tab, or locally with `./gradlew assembleDebug`.

# Contributing

## Reporting Issues
Found a bug or want to see a new feature in beBoard? Open an issue in the [issues section](../../issues) of this repository.

Before opening a new issue, please check:
 - **Does the issue already exist?** Search open and closed issues first.
 - **Does it also happen in HeliBoard?** If it does, it is likely best reported to [HeliBoard](https://github.com/HeliBorg/HeliBoard), so all users benefit from the fix.
 - **Is it a single topic?** If you want to suggest multiple things, open multiple issues.
 - **Did you use the issue template?** It helps a lot to have all the needed information.

## Translations
beBoard uses the translations made for HeliBoard on [Weblate](https://translate.codeberg.org/projects/heliboard/). Improving translations there helps both apps.

## Code Contribution
See [Contribution Guidelines](CONTRIBUTING.md)

# Links
* [Layout documentation](layouts.md) (technical info regarding layout customization)
* [Dictionaries](https://codeberg.org/Helium314/aosp-dictionaries), and [how to create your own](https://codeberg.org/Helium314/aosp-dictionaries#wordlist-information)
* [HeliBoard wiki](https://github.com/HeliBorg/HeliBoard/wiki), including FAQ and help on customizing layouts
* [Roccobot's Layout Maker](https://roccobot.github.io/HeliBoard-RLM/), a browser-based editor for json layout files

# License

beBoard (as a fork of HeliBoard, which is a fork of OpenBoard) is licensed under GNU General Public License v3.0.

 > Permissions of this strong copyleft license are conditioned on making available complete source code of licensed works and modifications, which include larger works using a licensed work, under the same license. Copyright and license notices must be preserved. Contributors provide an express grant of patent rights.

See repo's [LICENSE](/LICENSE) file.

Since the app is based on Apache 2.0 licensed AOSP Keyboard, an [Apache 2.0](LICENSE-Apache-2.0) license file is provided.
The icon is licensed under [Creative Commons BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). A [license file](LICENSE-CC-BY-SA-4.0) is also included.

# Credits
beBoard would not exist without [HeliBoard](https://github.com/HeliBorg/HeliBoard) and all of its [contributors](https://github.com/HeliBorg/HeliBoard/graphs/contributors). Nearly all of the code in this app comes from HeliBoard. HeliBoard's development is funded through the [NGI Mobifree Fund](https://nlnet.nl/mobifree) by [NLnet](https://nlnet.nl), and by donations from its users.

HeliBoard itself builds on:
- [OpenBoard](https://github.com/openboard-team/openboard)
- [AOSP Keyboard](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/)
- [LineageOS](https://review.lineageos.org/admin/repos/LineageOS/android_packages_inputmethods_LatinIME)
- [Simple Keyboard](https://github.com/rkkr/simple-keyboard)
- [Indic Keyboard](https://gitlab.com/indicproject/indic-keyboard)
- [FlorisBoard](https://github.com/florisboard/florisboard/)

Icon by [Fabian OvrWrt](https://github.com/FabianOvrWrt) with contributions from [The Eclectic Dyslexic](https://github.com/the-eclectic-dyslexic)
