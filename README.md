NOTE: mjprof is going into a major refactoring and currently not stable will be fixed until the end of the day 

MJProf
=======
MJProf is a command line monadic java profiler.

Introduction
=============
MJStack is Monadic jstack analysis tool set. It is a fancy way to say it analyzes jstack output using a series of simple building blocks
which can be composed together. I have thought for a long time how to analyze stack traces of java processes.  
After getting used to thinking with lambda expressions
and streams it suddenly occured to me that this may be a nice approach to stack trace analysis as well.

Motivation
==========
So, You are out there in the wild vs a production machine. All you have have in hand is the 'poor's man profiler': jstack.
You take one, two, three stack dumps and then you need to look at them manually inside an editor, vi or less etc....
If you done it enough you will probably know that it is a lot of manual work.



Run mjstack
===========
mjstack reads its standard input and writes to standard output so  you can pipe jstack output to it or stream a saved jstack
output (or even several) into it.
The following example will filter out all the threads which are not in RUNNABLE state.  
`jstack -l pid | ./mjs.sh contains/state,RUNNABLE/`  
The commands pass to mjstack are several building blocks concatenated with . (comma)
Parameters to building blocks are wrapped with / (instead of () {} or [] which are special chars in the shell) and seperated by ,


Building Blocks
===============
Filters
-------
* _**contains**/attr,string/_  - returns only threads which contains the string (regexp not supported)
* _**ncontains**/attr,string/_  - returns only threads which do no contains the string(regexp not supported)

Mappers
-------
* _**eliminate**/attr/_         - Removes a certain attribute e.g. eliminate/stack/
* _**sort**/attr/_              - Sorts based on attribute
* _**sortd**/attr/_             - Sorts based on attribute (descending order)
* _**keeptop**/int/_            - Returns at most n top stack frames of the stack
* _**keepbot**/int/_            - Returns at most n bottom stack frames of the stack
* _**trimbelow**/int/_          - Trim all stack frames below the first occurance of string 
* _**stackelim**/string/_       - Elminate stackframes from all stacks which contain string
* _**stackkeep**/string/_       - Keep only stackframes from all stacks which contain string
* _**pkgelim**_                 - Eliminates package name from stack frames
* _**ifnelim**_                 - Eliminates file name and line number  from stack frames
* _**nop**_                     - Do nothing.

Terminals
---------
* _**count**_                   - Counts number of threads
* _**tree**_                    - Creates a profiling tree based on all stack traces
* _**list**_                    - List the stack trace attributes
* _**group**/attr/_             - Group by a certain attribute

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
* _**prio**_            - Thread priority, a number
* _**stack**_           - The actual stack trace

You can also get the actual list of properties bu using the list monad.  
`jstack -l pid | ./mjs.sh list`

Examples
=============
jstack Original output:  
`jstack -l 38515 > mystack.txt`  
Keep only thread which their names contain ISCREAM:  
`jstack -l 38515  | ./mjs.sh contains/name,ISCREAM/`  
Sort them by state  
`cat mystack.txt  | ./mjs.sh contains/name,ISCREAM/.sort/state/`  
Eliminate the Locked Ownable Synchronizers Section  
`jstack -l 38515  | ./mjs.sh contains/name,ISCREAM/.sort/state/.eliminate/los/`  
Shorten stack traces to include only 10 last stack frames  
`jstack -l 38515  | ./mjs.sh contains/name,ISCREAM/.sort/state/.eliminate/los/.keeptop/10/`  
Count threads  
`jstack -l 38515  | ./mjs.sh contains/name,ISCREAM/.sort/state/.eliminate/los/.keeptop/10/.count`



Build mjstack
=============
Build mjstack with the following command line:  
`mvn clean package`
This will create a zip file in `target/dist/mjstack1.0-bin.zip` which contains everything you need.

 an object which represents attributes of thread including  call stack priority native thread id etc///

Write a plugin
===============

Write a Mapper:
- Implement JStackMapper interface that includes:
	map method: JStackMetadataStack map(JStackMetadataStack stck)
	JStackMetadataStack represents attributes of thread including call stack priority native thread id etc.
Write a Filter:
- implement JStackFilter interface that includes:
	filter method: boolean filter(JStackMetadataStack stck)
	JStackMetadataStack represents attributes of thread including call stack priority native thread id etc.
Write a Terminal:
- Implement JStackTerminal interface that includes:
	addStackDump method: void addStackDump(JStackDump jsd) 
	JStackDump represents the entire stack dump 
Write a comprator:
- Implement JStackComparator interface that includes:
	int compare(JStackMetadataStack o1, JStackMetadataStack o2)
	JStackMetadataStack represents attributes of thread including call stack priority native thread id etc.

For all the options above, add Plugin annotation: @Plugin.
The plugin annotation includes the following attributes:
	name - the command string
	paramTypes - the constractur parameters types.
	description - the string that will show in the help menu
For example:
	@Plugin(name="keeptop",paramTypes = {int.class}, description = "Returns at most n top stack frames of the stack")


Install a plugin
============
In order to install the plugin just drop your jar into the 'plugins' directory 
