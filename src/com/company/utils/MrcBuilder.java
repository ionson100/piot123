package com.company.utils;

/**
 * Утилита для извлечения и расшифровки цены из КИЗ (кода маркировки 29 символов) табачной продукции.
 * Цена закодирована в символах строки с использованием кастомного алфавита.
 */
public class MrcBuilder {

    // Алфавит, используемый для кодирования цены в КИЗ (64 символа)
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"%&'*+-./_,:;=<>?";
    private static final int BASE = ALPHABET.length(); // 64

    /**
     * Извлекает и декодирует цену из строки КИЗ (29 символа) (символы 21-25).
     *
     * @param cis Полный код маркировки (КИЗ) (пачка табачные изделия, размер кода 29 символов).
     * @return цена в рублях (например, 123.45), или -1 при ошибке
     */
    public static double getMrc(String cis) {
        if (cis == null || cis.length() < 29) {
            return -1;
        }

        String pricePart = cis.substring(21, 25); // 4 символа
        double result = 0;

        for (int i = 0; i < pricePart.length(); i++) {
            char c = pricePart.charAt(i);
            int index = ALPHABET.indexOf(c);

            if (index == -1) {
                return -1; // Символ не найден в алфавите
            }

            // Позиционная система счисления: base^position * digit
            double value = Math.pow(BASE, 3 - i) * index;
            result += value;
        }

        return result / 100.0; // Переводим копейки в рубли
    }
}