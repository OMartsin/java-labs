package lab4.order.handlers;

import lab4.city.City;
import lab4.city.CityDistanceCalculator;
import lab4.deliverycompany.DeliveryCompany;
import lab4.order.Order;
import lab4.shop.OnlineShop;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class OrderDeliveryTimeCalculator {
    public static int TIME_DIFFERENCE_HOURS_FOR_SWITCHING_TO_NEXT_DAY = 5;
    public static LocalDate calculateDeliveryDate(Order order) {
        OnlineShop shop = order.shop();
        City deliveryCity = order.cityOfDelivery();
        double distance = CityDistanceCalculator.calculateDistance(shop.getCity(), order.cityOfDelivery());
        LocalDate currDay = order.dateOfOrder();
        currDay = currDay.plusDays(1); // add one day for processing
        boolean isSent = false;

        HolidaysChecker holidaysChecker = new HolidaysChecker();
        while (distance > 0){
            if(!isSent){
                if(isShopWorkingDay(currDay,holidaysChecker, shop)){
                    isSent = true;
                    continue;
                }
            }
            if(isDeliveryWorkingDay(currDay, holidaysChecker, order.deliveryCompany())){
                if (order.isExpress()) {
                    distance -= order.deliveryCompany().distanceByDay() * 2;
                } else {
                    distance -= order.deliveryCompany().distanceByDay();
                }
            }
            currDay = currDay.plusDays(1);
        }

        return addTimeZoneDifference(currDay, shop, deliveryCity);
    }

    private static LocalDate addTimeZoneDifference(LocalDate currDay, OnlineShop shop, City deliveryCity) {
        var dateOfInterest = LocalDateTime.parse("2020-02-02T12:00");
        var hours = ChronoUnit.HOURS.between(dateOfInterest.atZone(shop.getCity().timeZone())
                ,dateOfInterest.atZone(deliveryCity.timeZone()));
        if(Math.abs(hours) > TIME_DIFFERENCE_HOURS_FOR_SWITCHING_TO_NEXT_DAY){
            return currDay.plusDays((int)Math.signum(hours));
        }
        return currDay;
    }

    private static boolean isShopWorkingDay(LocalDate dateToCheck, HolidaysChecker holidaysChecker, OnlineShop shop) {
        return holidaysChecker.isWorkingDay(dateToCheck) && shop.getShopSchedule().isWorkingDay(dateToCheck);
    }

    private static boolean isDeliveryWorkingDay(LocalDate dateToCheck, HolidaysChecker holidaysChecker,
                                                DeliveryCompany deliveryCompany) {
        return holidaysChecker.isWorkingDay(dateToCheck) && deliveryCompany.schedule().isWorkingDay(dateToCheck);
    }
}
