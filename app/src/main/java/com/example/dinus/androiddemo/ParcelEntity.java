package com.example.dinus.androiddemo;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelEntity implements Parcelable{

    private String name ;
    private int age;
    private float salary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public ParcelEntity(Parcel parcel){
        name = parcel.readString();
        salary = parcel.readFloat();
        age = parcel.readInt();
    }

    public ParcelEntity(String name, int age, float salary) {
        this.name = name;
        this.age = age;
        this.salary = salary;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeFloat(salary);
        dest.writeInt(age);

    }

    public static final Creator<ParcelEntity> CREATOR = new Creator<ParcelEntity>() {
        @Override
        public ParcelEntity createFromParcel(Parcel source) {
            return new ParcelEntity(source);
        }

        @Override
        public ParcelEntity[] newArray(int size) {
            return new ParcelEntity[size];
        }
    };


}
