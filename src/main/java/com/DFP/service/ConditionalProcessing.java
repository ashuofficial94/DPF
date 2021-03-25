package com.DFP.service;

import org.springframework.stereotype.Service;

@Service
public class ConditionalProcessing {
    public void departmentBasedBonus(String dept_no){
        switch (dept_no) {
            case "d001":
                System.out.println("Calculate Bonus - Marketing");

                break;
            case "d002":
                System.out.println("Calculate Bonus - Finance");
                break;
            case "d003":
                System.out.println("Calculate Bonus - Human Resources");
                break;
            case "d004":
                System.out.println("Calculate Bonus - Production");
                break;
            case "d005":
                System.out.println("Calculate Bonus - Development");
                break;
            case "d006":
                System.out.println("Calculate Bonus - Quality Management");
                break;
            case "d007":
                System.out.println("Calculate Bonus - Sales");
                break;
            case "d008":
                System.out.println("Calculate Bonus - Research");
                break;
            case "d009":
                System.out.println("Calculate Bonus - Customer Service");
                break;

        }
    }


    public void handleConditionalRequest(String value,String conditionType) {
        if(conditionType.equals("DEPT_BONUS")){
            departmentBasedBonus(value);
        }
    }
}
