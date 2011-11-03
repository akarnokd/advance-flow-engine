Advance Flow Engine README
==========================

Higly volatile development. Expect frequent changes, refactorings and so on.

Version 0.07.146 Notes
----------------------

Updated many ECC and flow engine components to work under a "working directory" instead of
the local directory.

By default, the working directory of the ECC is now ${user.home}/.advance-flow-editor-ws .

Added certificate file-based remote login into ECC, similarly to the FE.

The schemas and block registry are now default to the root classpath of the engine (root directory in the Jar).
Specifying external block registry and schemas is now optional.

The ECC configuration files are now stored in the working directory with name advance-ecc-*.xml

The ECC now parses the Flow editor's own login-info.xml in order to allow connecting to the
same engine when the ECC is run from inside the Flow Editor.

Version 0.06.139 Notes
----------------------

Removed Java 7 specific classes and methods. The engine is targeted at Java 6.

Fixed a few minor bugs.

Version 0.05.138 Notes
----------------------

Fixed several engine and communication related bugs.

Updated Reactive4Java library to version 0.95.1 due several bugs.

Updated the ECC to have more convenient realm management.

Version 0.05.133 Notes
----------------------

Fixed some bugs and functional issues with the flow engine.

Updated Reactive4Java library to version 0.95.


Version 0.05.129 Notes
----------------------

Added all screens to the Engine Control Center. The remaining features are the filter boxes (low priority).

Fixed some compilation and execution related issues with the flow engine.

Version 0.04.123 Notes
----------------------

Fixed user right exploits.

Added keystore management screens and routines.

Added remote login option to the control center.

Version 0.03.119 Notes
----------------------

Added Email details dialog.

Fixed datastore email record management.

Fixed some misspelling.

Extracted the update methods from the datastore to separate interface to allow 
SQL dialect specific implementation of the MERGE/REPLACE functionality.

Version 0.02.115 Notes
----------------------

Added more control center screens.

Fixed some bugs in the compiler.

Changed datastore API notification group type.

Version 0.02.112 Notes
----------------------

Added missing datastore API calls.

Added more Control Center screens.


Version 0.02.103 Notes
----------------------

Added E-mail related datastore classes.

Started on Flow Engine Control Center.

Fixed a few minor model bugs.


Version 0.01.097 Notes
----------------------

Some refactorings and added support classes.

Updated library versions.


Version 0.01.087 Notes
----------------------

2011-10-04 13:00 CET

Drastical refactoring of classes. The class names remained the same, but their location has changed.
This helps removing any package cyclic dependency and makes a more clear grouping of the code.

The SVN now contains a generated Javadoc of the Flow Engine.

All NetBeans and GWT related files have been removed and cleaned. Libraries are moved to the
/lib directory. 

