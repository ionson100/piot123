package com.company.validator;

class BaseValidator{

    /**
     * Проверка на продажу методами кассового по
     * @param km Полный код маркировки
     * @return true-sales false not sales
     */
    boolean  checkLocalSales(String km){
        return false;
    }

    /**
     * Получить идентификатор группы товара
     * @param km Полный код маркировки
     * @return идентификатор группы товаров или 0 - (группа не найдена)
     */
    public int getGroupId(String km){
        return 3;
    }


    public int getCapacityBlock(String km){
        return 10;
    }
}
