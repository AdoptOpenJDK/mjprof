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
_mvn install assembly:assembly_
This will create a zip file which contains everything you need.

Run mjstack
===========
mjstack reads its standard input and writes to standard output so  you can pipe jstack output to it or stream a saved jstack
output (or even several) into it.
The following example will filter out all the threads which are not in RUNNABLE state.

_jstack -l pid | ./mjs.sh contains/state,RUNNABLE/_

The commands pass to mjstack are several building blocks concatenated with .
Parameters to building blocks are wrapped with / (instead of () {} or [] which are special chars in the shell) and seperated by ,





Building Blocks
===============
Filters:
-------
* _contains/attr,string/_  - returns only threads which contains the string (regexp not supported)
* _ncontains/attr,string/_  - returns only threads which do no contains the string(regexp not supported)

Mappers:
-------
* eliminate/attr/         - removes a certain property e.g. eliminate/stack/
* sort/attr/              - sort based on property
* sortd/attr/             - sort based on property descending order
* keeptop/n/              - returns at most n top stack frames of the stack
* keepbottom/n/       - returns at most n top stack frames of the stack
* eltop/int/          -
* elbot/int/          -
* stackelim/string/   - elminate stackframes from the stack which do not contain string

Terminals:
---------
* count            - counts number of threads
* list             -list the stack trace attributes
* group/attr/      - group by a certain attribute


Properties
==========
Properties may change from stackdump to stack dump
* status          - the status of the thread
* nid             - native thread id ( a number)
* name            - name of thread
* state           - state of thread
* los             - The locked ownable synchronizers part of the stack trace
* daemon          - Whether the thread is a daemon or not
* tid             - The thread id (a number)
* prio            - Priority a number
* stack           - The actual stack trace


You can also get the actual list of properties
jstack -l pid | ./mjs.sh list
