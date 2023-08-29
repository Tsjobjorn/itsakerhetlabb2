package com.java22d.itsakerhet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PasswordGenerator {

    //testing t = new testing();
    //t.PrintPasswords(10);

    public void PrintPasswords(int amount){
        for (int i = 0; i < amount; i++) {
            System.out.println(GeneratePassword(true, false, false, 10));
        }
    }

    public String GeneratePassword(Boolean requiresCapitalLetter, Boolean requiresSymbol, Boolean numbersOnly,Integer passwordLength){
        StringBuilder generatedPassword = new StringBuilder();
        List<Character> charactersToUse = getCharacters(requiresCapitalLetter, requiresSymbol, numbersOnly);
        char[] numbers = "1234567890".toCharArray();
        char[] symbols = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/~".toCharArray();
        char[] bigLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        char[] smallLetters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        //List<char[]> bigList = List.of(numbers, symbols, smallLetters, bigLetters);
        List<Integer> dontUse = new ArrayList<>();

        boolean containsSymbol = false;
        boolean containsCapitalLetter = false;
        boolean containsNumber = false;

        for (int i = 0; i < passwordLength; i++) {
            char nextChar = charactersToUse.get(getRandomNumber(charactersToUse.size()-1));
            if (Character.isDigit(nextChar)){
                containsNumber = true;
            }
            if (Character.isUpperCase(nextChar)){
                containsCapitalLetter = true;
            }
            if ("!@#$%^&*()-_=+[]{}|;:'\",.<>?/~".contains(Character.toString(nextChar))){
                containsSymbol = true;
            }
            generatedPassword.append(nextChar);
        }


        if (requiresCapitalLetter && !containsCapitalLetter){
            int nextRandom = getRandomNotUsed(generatedPassword.length(), dontUse);
            dontUse.add(nextRandom);

            generatedPassword.setCharAt(nextRandom, bigLetters[getRandomNumber(bigLetters.length)]);
            containsNumber = true;
        }
        if (requiresSymbol && !containsSymbol){
            int nextRandom = getRandomNotUsed(generatedPassword.length(), dontUse);
            dontUse.add(nextRandom);

            generatedPassword.setCharAt(nextRandom, symbols[getRandomNumber(symbols.length)]);
            containsSymbol = true;
        }
        if (!containsNumber){
            int nextRandom = getRandomNotUsed(generatedPassword.length(), dontUse);
            dontUse.add(nextRandom);

            generatedPassword.setCharAt(nextRandom, numbers[getRandomNumber(numbers.length)]);
            containsNumber = true;
        }
        System.out.println(generatedPassword);
        return generatedPassword.toString();
    }
    private int getRandomNotUsed(int max, List<Integer> used){
        int temp = getRandomNumber(max);

        if (used.contains(temp)){
            temp = getRandomNotUsed(max, used);
        }
        return temp;
    }
    private int getRandomNumber(int max){
        Random random = new Random();
        return random.nextInt(max+1);
    }

    private List<Character> toCharacterArray(char[] charArray) {
        List<Character> result = new ArrayList<>();
        for (int i = 0; i < charArray.length; i++) {
            result.add(charArray[i]);
        }
        return result;
    }
    private List<Character> getCharacters(Boolean capitalLetters, Boolean symbols, Boolean numbersOnly){
        if (numbersOnly){
            return toCharacterArray(getNumbers());
        }

        List<Character> temp = toCharacterArray(getNumbers());
        temp.addAll((toCharacterArray(getSmallLetters())));


        //if (capitalLetters){
            temp.addAll(toCharacterArray(getBigLetters()));
        //}
        //if (symbols){
            temp.addAll(toCharacterArray(getSymbols()));
        //}

        return temp;
    }

    private char[] getNumbers(){
        return "1234567890".toCharArray();
    }

    private char[] getSmallLetters(){
        return "abcdefghijklmnopqrstuvwxyz".toCharArray();
    }

    private char[] getBigLetters(){
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    }

    private char[] getSymbols(){
        return "!@#$%^&*()-_=+[]{}|;:'\",.<>?/~".toCharArray();
    }
}
