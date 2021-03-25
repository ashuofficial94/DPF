package com.DFP.controller;




class Human{
    protected String Name;
    protected String Address;

    public Human(String name, String address) {
        Name = name;
        Address = address;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}

class Student extends Human{
    public int id;
    private int fees;

    public Student(String name, String address, int id, int fees) {
        super(name, address);
        this.id = id;
        this.fees = fees;
    }
    public void displaydata(){
        System.out.println(id+fees+Name+Address);
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFees() {
        return fees;
    }

    public void setFees(int fees) {
        this.fees = fees;
    }
}
class Teacher extends Human{
    private int id;
    private int salary;

    public Teacher (String name,String address, int abc,int salary ){
        super(name,address);
        this.id = abc;
        this.salary = salary;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }
}
public class Test {

    public static void main(String[] args) {
        System.out.println("Hello, World!");
        Student s1 = new Student("Shivam","abc",123,500);
        System.out.println(s1.id);
        s1.displaydata();

    }
}
