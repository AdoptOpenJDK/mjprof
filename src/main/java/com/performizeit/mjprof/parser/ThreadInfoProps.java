/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjprof.parser;

/**
 * Constants for thread property names used throughout the system.
 */
public interface ThreadInfoProps {
    // Thread identification properties
    String NAME = "name";      // the name of the thread
    String TID = "tid";        // java thread id
    String NID = "nid";        // native thread id
    String DAEMON = "daemon";  // daemon true/false
    
    // Thread priority/state properties
    String PRIO = "prio";      // priority
    String STATUS = "status";  // thread status
    String STATE = "state";    // thread state
    
    // Performance properties
    String CPUNS = "cpu_ns";   // amount of cpu consumed by thread in nanoseconds
    String WALL = "wall_ms";   // wall clock time in milliseconds
    String CPU_PREC = "%cpu";  // CPU percentage
    
    // Stack/profile properties
    String STACK = "stack";    // the stack of the thread or the profile of more than one thread
    String LOS = "los";        // locked ownable synchronizers
    String COUNT = "count";    // number of actual stacks this profile represents
}