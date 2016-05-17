package test;


import edu.citadel.cprlvm.assembler.optimize.OptimizationUtil;


public class TestShiftAmount
  {

    public static void main(String[] args)
      {
        int n = -5;
        byte shiftAmount = OptimizationUtil.getShiftAmount(n);
        System.out.println("n = " + n + ", shiftAmount = " + shiftAmount);

        n = 0;
        shiftAmount = OptimizationUtil.getShiftAmount(n);
        System.out.println("n = " + n + ", shiftAmount = " + shiftAmount);

        n = 1;
        for (int exp = 1;  exp <= 32;  ++exp)
          {
            n = 2*n;
            shiftAmount = OptimizationUtil.getShiftAmount(n);
            System.out.println("n = " + n + ", shiftAmount = " + shiftAmount);
          }
      }
  }
