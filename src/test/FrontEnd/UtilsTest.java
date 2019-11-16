package FrontEnd;

import static org.junit.Assert.assertEquals;

 import FrontEnd.Utils;
 import org.junit.Test;

 public class UtilsTest {

     @Test
     public void givenEmptyArray_callingGetMajority_returnsFAIL()
     {
         String [] input = {"ACK"};
         String expectedResult = "NO_MAJORITY";
         assertEquals(expectedResult, Utils.getMajority(input));
     }

     @Test
     public void givenArrayWithOneElement_callingGetMajority_returnsFAIL()
     {
         String [] input = {"ACK", "something"};
         String expectedResult = "NO_MAJORITY";
         assertEquals(expectedResult, Utils.getMajority(input));
     }

     @Test
     public void givenArrayWithTwoUnequalElements_callingGetMajority_returnsFAIL()
     {
         String [] input = {"ACK", "something", "somethingElse"};
         String expectedResult = "NO_MAJORITY";
         assertEquals(expectedResult, Utils.getMajority(input));
     }

     @Test
     public void givenArrayWithTwoEqualElements_callingGetMajority_returnsTheMajority()
     {
         String [] input = {"ACK", "something", "something"};
         String expectedResult = "something";
         assertEquals(expectedResult, Utils.getMajority(input));
     }

     @Test
     public void givenArrayWithThreeElementsTheSame_callingGetMajority_returnsTheMajority()
     {
         String [] input = {"ACK", "something", "something", "something"};
         String expectedResult = "something";
         assertEquals(expectedResult, Utils.getMajority(input));
     }

     @Test
     public void givenArrayWithThreeElementsMajority_callingGetMajority_returnsTheMajority()
     {
         String [] input = {"ACK", "something", "something", "somethingElse"};
         String expectedResult = "something";
         assertEquals(expectedResult, Utils.getMajority(input));
     }

     @Test
     public void givenArrayWithThreeElementsWithoutMajority_callingGetMajority_returnsFAIL()
     {
         String [] input = {"ACK", "something", "somethingElse", "somethingElse2"};
         String expectedResult = "NO_MAJORITY";
         assertEquals(expectedResult, Utils.getMajority(input));
     }

     @Test
     public void givenArrayWithFourElementsWithoutMajority_callingGetMajority_returnsFAIL()
     {
         String [] input = {"ACK", "something","something", "somethingElse", "somethingElse"};
         String expectedResult = "NO_MAJORITY";
         assertEquals(expectedResult, Utils.getMajority(input));
     }

     @Test
     public void givenArrayWithFourElementsWithMajority_callingGetMajority_returnsMajority()
     {
         String [] input = {"ACK", "something","something", "something", "somethingElse"};
         String expectedResult = "something";
         assertEquals(expectedResult, Utils.getMajority(input));
     }

 }
