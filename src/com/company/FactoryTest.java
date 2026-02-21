package com.company;

import com.company.models.MInItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FactoryTest {
    HashMap<String, MInItems> hashMap=new HashMap<>();
    public FactoryTest initFactory(){
        {
            MInItems items=new MInItems();
            items.km="0104670540176099215'W9Um\u001D93dGVz";//
            items.idCase="5.1";
            items.descriptionCase ="Запрет продажи товара при отсутствии в информационной системе мониторинга сведений о его нанесении";
            hashMap.put("5.1",items);
        }
        {
            MInItems items=new MInItems();
            items.km="0104670540176099215LnOjv\u001D93dGVz";
            items.idCase="5.2";
            items.descriptionCase ="Запрет продажи товара при отсутствии в информационной системе мониторинга сведений о его вводе в оборот";
            hashMap.put("5.2",items);
        }

        {
            MInItems items=new MInItems();
            items.km="010462930887704421DzkcYt2\u001D8005090000\u001D93dGVz";
            items.idCase="5.3";
            items.descriptionCase ="Успешная продажа табачной продукции (цена до 100 руб. за пачку для прохождения на МГМ)";
            hashMap.put("5.3",items);
        }

        {
            MInItems items=new MInItems();
            items.km="010462930887704421DzkcWqS\u001D8005177000\u001D93dGVz";
            items.idCase="5.4";
            items.descriptionCase ="Успешная продажа табачной продукции (цена более 135 руб.\n" +
                    "за пачку для соблюдения законодательства РФ), потребительская или\n" +
                    "групповая упаковка которых относится к временно непрослеживаемой\n" +
                    "(т.н. «серая зона»)";
            hashMap.put("5.4",items);
        }

        {
            MInItems items=new MInItems();
            items.km="0104670540176099215NN*cM\u001D93dGVz";
            items.idCase="5.5";
            items.descriptionCase ="Запрет продажи товара, который на момент проверки выведен из оборота";
            hashMap.put("5.5",items);
        }

        {
            MInItems items=new MInItems();
            items.km="0104602220006549215opFcmK\u001D93dGVz";
            items.idCase="5.6";
            items.descriptionCase ="Запрет продажи товара, заблокированного или приостановленного для реализации по решению органов власти";
            hashMap.put("5.6",items);
        }

        {
            MInItems items=new MInItems();
            items.km="0104670540176099215<pGKy\u001D93dGVz";
            items.idCase="5.7";
            items.descriptionCase ="Продажа товара с истекшим сроком годности";
            hashMap.put("5.7",items);
        }

        {
            MInItems items=new MInItems();
            items.km="010461013628057121%798DM%\u001D8005090000\u001D93dGVz";
            items.idCase="5.8";
            items.descriptionCase ="Успешная продажа блока сигарет (папирос) по максимальной\n" +
                    "розничной цене (цена до 100 руб. за пачку для прохождения на МГМ), указанной в коде маркировки";
            hashMap.put("5.8",items);
        }

        {
            MInItems items=new MInItems();
            items.km="010461013628057121%008iVk\u001D8005180000\u001D93dGVz";
            items.idCase="5.9";
            items.descriptionCase ="Успешная продажа блока сигарет (папирос) по максимальной\n" +
                    "розничной цене (цена более 135 руб. за пачку для соблюдения\n" +
                    "законодательства РФ), указанной в коде маркировки";
            hashMap.put("5.9",items);
        }

        {
            MInItems items=new MInItems();
            items.km="04601653035829H;dV)bFABVUdGVz";
            items.idCase="5.10";
            items.descriptionCase ="Продажа пачки сигарет (папирос) по максимальной\n" +
                    "розничной цене (цена до 100 руб. за пачку для прохождения на МГМ), указанной в коде маркировки";
            hashMap.put("5.10",items);
        }

        {
            MInItems items=new MInItems();
            items.km="04601653035829H;dV)bFACVUdGVz";
            items.idCase="5.11";
            items.descriptionCase ="Продажа пачки сигарет (папирос) по максимальной\n" +
                    "розничной цене (цена более 135 руб. за пачку для соблюдения законодательства РФ), указанной в коде маркировки";
            hashMap.put("5.11",items);
        }
        {
            MInItems items=new MInItems();
            items.km="04601653035829H;vE)bFABBUdGVz";
            items.idCase="5.12";
            items.descriptionCase ="Продажа товара, сведения о маркировке средствами\n" +
                    "идентификации которого отсутствуют в информационной системе мониторинга";
            hashMap.put("5.12",items);
        }



        {
            MInItems items=new MInItems();
            items.km="0104670540176099215<pGKy\u001D93DGVz";
            items.idCase="5.14";
            items.descriptionCase ="Запрет продажи товара с некорректным кодом проверки";
            hashMap.put("5.14",items);
        }

        {
            MInItems items=new MInItems();
            items.km="0104607010350246215kRdG-1%2(UmV\u001D93dGVz";
            items.idCase="5.17";
            items.descriptionCase ="Продажа товара в режиме офлайн, отсутствующего в\n" +
                    "черном списке локального модуля. ";
            hashMap.put("5.17",items);
        }

        {
            MInItems items=new MInItems();
            items.km="0104607010350246215kRdG-1%W(Umn\u001D93dGVz";
            items.idCase="5.18";
            items.descriptionCase ="Продажа товара в режиме проверки офлайн,\n" +
                    "отсутствующего в черном списке ЛМ ЧЗ (ответ от ГИС МТ 5 секунд)";
            hashMap.put("5.18",items);
        }

        {
            MInItems items=new MInItems();
            items.km="0104602220006549215opRcmR\u001D93dGVz";
            items.idCase="5.19";
            items.descriptionCase ="Запрет продажи товара в режиме проверки офлайн, присутствующего в черном списке ЛМ ЧЗ";
            hashMap.put("5.19",items);
        }

        {
            MInItems items=new MInItems();
            items.km="0104607010350246215kRdG-X%W(Rnb\u001D93dGVz";
            items.idCase="5.20";
            items.descriptionCase ="Запрет продажи товара в режиме проверки офлайн, присутствующего в черном списке ЛМ ЧЗ (ответ от ГИС МТ 5 секу";
            hashMap.put("5.20",items);
        }
        {
            MInItems items=new MInItems();
            items.km="0104670540176099215LpGKy\u001D93dGVz";
            items.idCase="5.21";
            items.descriptionCase ="Сканирование кода маркировки, который возвращает 203 - ошибку и переводит ТС ПИоТ в аварийный режим";
            hashMap.put("5.21",items);
        }

        {
            MInItems items=new MInItems();
            items.km="01046700190342882151aj\"K>X+mFcP\u001D93dGVz";
            items.idCase="5.22";
            items.descriptionCase ="Запрет продажи товара, когда РД признан прекращенным или недействительным \n" +
                    "по решению государственного органа контроля (надзора) за соблюдением требований технических регламентов";
            hashMap.put("5.22",items);
        }

        {
            MInItems items=new MInItems();
            items.km="0108607405401894215cC3O4\u001D93dGVz";
            items.idCase="5.23";
            items.descriptionCase ="Запрет продажи товара при аннулированном ВСД";
            hashMap.put("5.23",items);
        }

        {
            MInItems items=new MInItems();
            items.km="04601653035829H;dV)bFADI8dGVz";
            items.idCase="5.24";
            items.descriptionCase ="Запрет продажи пачки сигарет (папирос) по минимальной розничной цене";
            hashMap.put("5.24",items);
        }

        {
            MInItems items=new MInItems();
            items.km="010461013628057121/798DM%\u001D8005199000\u001D93dGVz";
            items.idCase="5.25";
            items.descriptionCase ="Запрет продажи блока сигарет (папирос) по минимальной розничной цене";
            hashMap.put("5.25",items);
        }

        {
            MInItems items=new MInItems();
            items.km="00840147505712Zz;ZnRbAAAAdGVz";
            items.idCase="5.26";
            items.descriptionCase ="Запрет продажи НСП по минимальной розничной цене";
            hashMap.put("5.26",items);
        }

        {
            MInItems items=new MInItems();
            items.km="0104670540176099215'W9Um\u001D93dGVz";//
            hashMap.put("00",items);
        }
        return this;


    }
    public List<MInItems> buildRequest(String ... cas ) throws Exception {
        List<MInItems> mInItems=new ArrayList<>(cas.length);
        for (String key : cas) {
            MInItems value=hashMap.get(key);
            if(value==null){
                throw  new Exception("Кейса:"+key+" не сушествует в пуле");
            }
            mInItems.add(value);
        }

        return mInItems;
    }
}
