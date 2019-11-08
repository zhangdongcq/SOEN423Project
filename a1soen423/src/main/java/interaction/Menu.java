package interaction;

import user.User;
import utility.IdValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Menu {

    public static ArrayList<String> getRequest(User user, Scanner scanner)
    {
        ArrayList<String> menuArguments;
        if(user.isAdmin()) {
            menuArguments = getAdminMenuArguments(scanner, user);
        } else {
            menuArguments = Menu.getPatientMenuArguments(scanner, user);
        }
        return menuArguments;
    }

    private static ArrayList<String> logout()
    {
        ArrayList<String> arguments = new ArrayList<>(1);
        arguments.add(0, "logout");
        System.out.println("You have selected > Log Out");
        return arguments;
    }

    private static ArrayList<String> bookAppointment(Scanner scanner, User user)
    {
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList("","","",""));
        arguments.set(0, "bookAppointment");
        System.out.println("You have selected > Book Appointment");
        if(user.isAdmin())
        {
            while(!IdValidator.isPatientID(arguments.get(1)) ||
                    !IdValidator.isValidAppointmentID(arguments.get(2)) ||
                    !IdValidator.isValidAppointmentType(arguments.get(3)))
            {
                System.out.println("Enter > PatientID AppointmentID AppointmentType > Where AppointmentType in {Surgeon, Physician, Dental} ");
                String[] userInput = scanner.nextLine().split(" ");
                final int expectedNumberOfItems = 3;
                if(userInput.length < expectedNumberOfItems)
                    continue;
                setCallingArguments(arguments, userInput, expectedNumberOfItems, false);
            }
        } else {
            arguments.set(1,user.getUserID());
            while(!IdValidator.isValidAppointmentID(arguments.get(2)) ||
                    !IdValidator.isValidAppointmentType(arguments.get(3)))
            {
                System.out.println("Enter > AppointmentID AppointmentType > Where AppointmentType in {Surgeon, Physician, Dental} ");
                String[] userInput = scanner.nextLine().split(" ");
                final int expectedNumberOfItems = 2;
                if(userInput.length < expectedNumberOfItems)
                    continue;
                setCallingArguments(arguments, userInput, expectedNumberOfItems, true);
            }

        }
        return arguments;
    }

    private static ArrayList<String> getAppointmentSchedule(Scanner scanner, User user)
    {
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList("", ""));
        arguments.set(0, "getAppointmentSchedule");
        System.out.println("You have selected > Get Appointment Schedule");
        if(user.isAdmin())
        {
            while(!IdValidator.isPatientID(arguments.get(1)))
            {
                System.out.println("Enter > PatientID");
                String[] userInput = scanner.nextLine().split(" ");
                final int expectedNumberOfItems = 1;
                if(userInput.length < expectedNumberOfItems)
                    continue;
                setCallingArguments(arguments, userInput, expectedNumberOfItems, false);
            }
        } else {
            arguments.set(1, user.getUserID());
        }
        return arguments;
    }

    private static ArrayList<String> cancelAppointment(Scanner scanner, User user)
    {
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList("", "", ""));
        arguments.set(0, "cancelAppointment");
        System.out.println("You have selected > Cancel Appointment");
        if(user.isAdmin())
        {
            while(!IdValidator.isPatientID(arguments.get(1)) ||
                    !IdValidator.isValidAppointmentID(arguments.get(2)))
            {
                System.out.println("Enter > PatientID AppointmentID");
                String[] userInput = scanner.nextLine().split(" ");
                final int expectedNumberOfItems = 2;
                if(userInput.length < expectedNumberOfItems)
                    continue;
                setCallingArguments(arguments, userInput, expectedNumberOfItems, false);
            }
        } else {
            System.out.println("Enter > AppointmentID");
            arguments.set(1, user.getUserID());
            while(!IdValidator.isValidAppointmentID(arguments.get(2)))
            {
                String[] userInput = scanner.nextLine().split(" ");
                final int expectedNumberOfItems = 1;
                if(userInput.length < expectedNumberOfItems)
                    continue;
                setCallingArguments(arguments, userInput, expectedNumberOfItems, true);
            }
        }
        return arguments;
    }

    private static ArrayList<String> addAppointment(Scanner scanner)
    {
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList("", "", "", ""));
        arguments.add(0, "addAppointment");
        System.out.println("You have selected > Add Appointment");
        while(!IdValidator.isValidAppointmentID(arguments.get(1)) ||
            !IdValidator.isValidAppointmentType(arguments.get(2)) ||
            Integer.parseInt(arguments.get(3)) < 0)
        {
            System.out.println("Enter > AppointmentID AppointmentType Capacity > Where AppointmentType in {Surgeon, Physician, Dental}");
            String[] userInput = scanner.nextLine().split(" ");
            final int expectedNumberOfItems = 3;
            if(userInput.length < expectedNumberOfItems)
                continue;
            setCallingArguments(arguments, userInput, expectedNumberOfItems, false);
        }
        return arguments;
    }

    private static ArrayList<String> removeAppointment(Scanner scanner)
    {
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList("", "", ""));
        arguments.add(0, "removeAppointment");
        System.out.println("You have selected > Remove Appointment");
        while(!IdValidator.isValidAppointmentID(arguments.get(1)) ||
                !IdValidator.isValidAppointmentType(arguments.get(2)))
        {
            System.out.println("Enter > AppointmentID AppointmentType > Where AppointmentType in {Surgeon, Physician, Dental}");
            String[] userInput = scanner.nextLine().split(" ");
            final int expectedNumberOfItems = 2;
            if(userInput.length < expectedNumberOfItems)
                continue;
            setCallingArguments(arguments, userInput, expectedNumberOfItems, false);
        }
        return arguments;
    }

    private static ArrayList<String> swapAppointment(Scanner scanner, User user)
    {
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList("", "", "","","",""));
        arguments.add(0, "swapAppointment");
        System.out.println("You have selected > Swap Appointment");
        if(user.isAdmin()) {
            while (!IdValidator.isValidAppointmentType(arguments.get(3)) ||
                    !IdValidator.isValidAppointmentType(arguments.get(5)) ||
                    !IdValidator.isValidUserID(arguments.get(1)) || !IdValidator.isPatientID(arguments.get(1)) ||
                    !IdValidator.isValidAppointmentID(arguments.get(2)) ||
                    !IdValidator.isValidAppointmentID(arguments.get(4))) {
                System.out.println("Enter > PatientID OldAppointmentID OldAppointmentType NewAppointmentId NewAppointmentType >" +
                        " Where AppointmentType in {Surgeon, Physician, Dental}");
                String[] userInput = scanner.nextLine().split(" ");
                final int expectedNumberOfItems = 5;
                if(userInput.length < expectedNumberOfItems)
                    continue;
                setCallingArguments(arguments, userInput, expectedNumberOfItems, false);
            }
        } else {
            arguments.set(1, user.getUserID());
            while(!IdValidator.isValidAppointmentType(arguments.get(5)) ||
                    !IdValidator.isValidAppointmentType(arguments.get(3)) ||
                    !IdValidator.isValidAppointmentID(arguments.get(2)) ||
                    !IdValidator.isValidAppointmentID(arguments.get(4)))
            {
                System.out.println("Enter > OldAppointmentID OldAppointmentType NewAppointmentId NewAppointmentType >" +
                        " Where AppointmentType in {Surgeon, Physician, Dental}");
                String[] userInput = scanner.nextLine().split(" ");
                final int expectedNumberOfItems = 4;
                if(userInput.length < expectedNumberOfItems)
                    continue;
                setCallingArguments(arguments, userInput, expectedNumberOfItems, true);
            }
        }
        return arguments;
    }

    private static ArrayList<String> listAppointmentAvailability(Scanner scanner)
    {
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList("", ""));
        arguments.add(0, "listAppointmentAvailability");
        System.out.println("You have selected > List Appointment Availability");
        while(!IdValidator.isValidAppointmentType(arguments.get(1)))
        {
            System.out.println("Enter > AppointmentType > Where AppointmentType in {Surgeon, Physician, Dental}");
            String[] userInput = scanner.nextLine().split(" ");
            final int expectedNumberOfItems = 1;
            if(userInput.length < expectedNumberOfItems)
                continue;
            setCallingArguments(arguments, userInput, expectedNumberOfItems, false);
        }
        return arguments;
    }

    private static ArrayList<String> getUserArguments(int menuOption, Scanner scanner, User user)
    {
        ArrayList<String> arguments;
        switch (menuOption)
        {
            case 0:
                arguments = logout();
                break;
            case 1:
                arguments = bookAppointment(scanner, user);
                break;
            case 2:
                arguments = getAppointmentSchedule(scanner, user);
                break;
            case 3:
                arguments = cancelAppointment(scanner, user);
                break;
            case 4:
                arguments = swapAppointment(scanner, user);
                break;
            case 5:
                arguments = addAppointment(scanner);
                break;
            case 6:
                arguments = removeAppointment(scanner);
                break;
            case 7:
                arguments = listAppointmentAvailability(scanner);
                break;
            default:
                arguments = new ArrayList<>(0);
                System.out.println("Error: No such menu option available");
        }
        return arguments;
    }

    private static ArrayList<String> getPatientMenuArguments(Scanner scanner, User user)
    {
        System.out.println("Select one of the following options:");
        int answer = -1;
        while (answer < 0 || answer > 4) {
            try {
                showMainMenu();
                answer = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e)
            {
                scanner.nextLine();
                System.out.println("Exception: item entered was not an integer");
                answer = -1;
            }
        }
        return getUserArguments(answer,scanner, user);
    }

    private static ArrayList<String> getAdminMenuArguments(Scanner scanner, User user)
    {
        System.out.println("Select one of the following options:");
        int answer = -1;
        while (answer < 0 || answer > 7) {
            showMainMenu();
            System.out.println("5. Add Appointment");
            System.out.println("6. Remove Appointment");
            System.out.println("7. List Appointment Availability");
            try {
                answer = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e)
            {
                scanner.nextLine();
                System.out.println("Exception: item entered was not an integer");
                answer = -1;
            }
        }
        return getUserArguments(answer, scanner, user);
    }

    private static void showMainMenu()
    {
        System.out.println("0. Log Out");
        System.out.println("1. Book Appointment");
        System.out.println("2. Get Appointment Schedule");
        System.out.println("3. Cancel Appointment");
        System.out.println("4. Swap Appointment");
    }

    private static void setCallingArguments(ArrayList<String> arguments, String[] userInput, int itemsNumber, boolean isPatient)
    {
        for(int i = 0 ; i< itemsNumber ; i++)
        {
            if(isPatient)
                arguments.set(i + 2, userInput[i]);
            else
                arguments.set(i + 1, userInput[i]);
        }
    }
}


/*
MTLA121212 Dental
MTLA121212 Dental MTLA121212 Surgeon
MTLA121212 Surgeon QUEA111111 Physician
 */
