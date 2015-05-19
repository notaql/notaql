/*
 * Copyright 2015 by Thomas Lottermann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package notaql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
* Created by thomas on 26.01.15.
*/
public class Person {
    private String name = null;
    private String born = null;
    private String school = null;
    private String cmpny = null;
    private String salary = null;
    private Map<String, String> children = new HashMap<String, String>();

    public Person(String name, String born, String cmpny, String salary, Map<String, String> children) {
        this.name = name;
        this.born = born;
        this.cmpny = cmpny;
        this.salary = salary;
        this.children = children;
    }

    public Person(String name, String born, String school) {
        this.name = name;
        this.born = born;
        this.school = school;
    }

    public String getName() {
        return name;
    }

    public String getBorn() {
        return born;
    }

    public String getSchool() {
        return school;
    }

    public String getCmpny() {
        return cmpny;
    }

    public String getSalary() {
        return salary;
    }

    public Map<String, String> getChildren() {
        return children;
    }

    public static List<Person> generatePersons() {
        final List<Person> persons = new LinkedList<Person>();

        Map<String, String> children = new HashMap<String, String>();
        children.put("Susi", "10");
        children.put("John", "5");

        persons.add(new Person("Peter", "1967", "IBM", "50000", children));

        children = new HashMap<>();
        children.put("Susi", "20");
        children.put("John", "10");

        persons.add(new Person("Kate", "1968", "IBM", "60000", children));

        persons.add(new Person("Susi", "1989", "Eton School of Eton Business"));
        persons.add(new Person("John", "1991", "Eton"));

        return persons;
    }
}
