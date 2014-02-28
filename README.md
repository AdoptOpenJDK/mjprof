mjstack
=======
MJStack is Monadic jstack analysis tool set.

Introduction
=============
MJStack is Monadic jstack analysis tool set. It is a fancy way to say it analyzes jstack output using a series of simple building blocks
which can be composed together. I have thought for a long time how to analyze stack traces of java processes. Most of the tools like
thread dump analyzer give a very rigid UI which does not give you what you want. After getting used to thinking with lambda expressions
and streams it suddenly occured to me that this may be a nice approach to stack trace analysis as well.

Motivation
==========
So, You are out there in the wild vs a production machine. All you have have in hand is the 'poor's man profiler': jstack.
You take one, two, three stack dumps and then you need to look at them manually inside an editor, vi or less etc....
If you done it enough you will probably know that it is a lot of manual work.

Build mjstack
=============
Build mjstack with the following command line:

`mvn install assembly:assembly`

This will create a zip file which contains everything you need.

Run mjstack
===========
mjstack reads its standard input and writes to standard output so  you can pipe jstack output to it or stream a saved jstack
output (or even several) into it.
The following example will filter out all the threads which are not in RUNNABLE state.

`jstack -l pid | ./mjs.sh **contains**/state,RUNNABLE/`

The commands pass to mjstack are several building blocks concatenated with .
Parameters to building blocks are wrapped with / (instead of () {} or [] which are special chars in the shell) and seperated by ,





Building Blocks
===============
Filters
-------
* _**contains**/attr,string/_  - returns only threads which contains the string (regexp not supported)
* _**ncontains**/attr,string/_  - returns only threads which do no contains the string(regexp not supported)

Mappers
-------
* _**eliminate**/attr/_         - removes a certain property e.g. eliminate/stack/
* _**sort**/attr/_              - sort based on property
* _**sortd**/attr/_             - sort based on property descending order
* _**keeptop**/n/_              - returns at most n top stack frames of the stack
* _**keepbottom**/n/_           - returns at most n top stack frames of the stack
* _**eltop**/int/_              -
* _**elbot**/int/_              -
* _**stackelim**/string/_       - elminate stackframes from the stack which do not contain string

Terminals
---------
* _**count**_            - counts number of threads
* _**list**_             -list the stack trace attributes
* _**group**/attr/_      - group by a certain attribute


Properties
----------
Properties may change from one dump to another and the can also be eliminated by **mjstack**.
Following is the list of usual properties
* _**status**_          - the status of the thread
* _**nid**_             - native thread id ( a number)
* _**name**_            - name of thread
* _**state**_           - state of thread
* _**los**_            - The locked ownable synchronizers part of the stack trace
* _**daemon**_          - Whether the thread is a daemon or not
* _**tid**_             - The thread id (a number)
* _**prio**_            - Priority a number
* _**stack**_           - The actual stack trace


You can also get the actual list of properties bu using the list monad.

`jstack -l pid | ./mjs.sh list`
