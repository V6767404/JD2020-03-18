package by.it.gutkovsky.jd02_03;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

class Buyer extends Thread implements IBuyer, IUseBacket {

    private boolean waitSatet = false;

    public void setWaitSatet(boolean waitSatet) {
        this.waitSatet = waitSatet;
    }

    private Basket basket;

    public Basket getBasket() {
        return basket;
    }

    private static final Semaphore semaphore = new Semaphore(20);
    private static final Semaphore basketSemaphore = new Semaphore(50);

    private final int goodsQuantityInTheBasket = Helper.getRandom(1, 4); // goods quantity in buyers shopping list
    private final boolean pensioner;
    private static final double PENSIONER_FACTOR = 1.5;

    public boolean isPensioner() {
        return pensioner;
    }

    public Buyer(int number) {
        super("Buyer № " + number + " ");
        this.pensioner = false;
        Manager.customerComeIn();
    }

    public Buyer(int number, boolean pensioner) {
        super("Buyer № " + number + "(The Buyer is pensioner)" + " ");
        this.pensioner = pensioner;
        Manager.customerComeIn();
    }

    @Override
    public void run() {
        enterToMarket();
        try {
            System.out.println(this + " waiting for a basket");
            basketSemaphore.acquire(); //  semaphore for baskets: only for 50 acquire it is allowed to perform the program,
            // the rest will wait till basket will free
            takeBacket();
            try {
                semaphore.acquire(); // semaphore for buyers in shop: only 20 buyers can be in shop simultaneously
                chooseGoods(); // in this method buyer choose goods and put them into the basket
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                semaphore.release();
            }

            goToQueue();
            goOut();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            basketSemaphore.release();
        }


    }

    private void sleepMethod(int start, int stop, boolean pensioner) {
        int timeout;
        if (pensioner) {
            timeout = (int) (Helper.getRandom(start, stop) * PENSIONER_FACTOR);
        } else {
            timeout = Helper.getRandom(start, stop);
        }
        Helper.sleep(timeout);
    }

    @Override
    public void enterToMarket() {
        System.out.println(this + "enter to shop");
    }

    @Override
    public void takeBacket() {
        sleepMethod(500, 2000, pensioner);
        if (basket == null) {
            basket = new Basket();
        }
        System.out.println(this + "take a basket");
    }

    @Override
    public void chooseGoods() {
        System.out.println(this + "started to choose goods");
        Map<String, Double> shoppingList = new HashMap<>();

        for (int i = 0; i < goodsQuantityInTheBasket; i++) {
            sleepMethod(500, 2000, pensioner);

            //choose goods from goods shelf
            String goodsName = "Product" + Helper.getRandom(1, GoodsShelf.LIST_OF_GOODS_ON_SHELF.size()); // key for Map goodsOnShelf - chosenGoods
            double price = GoodsShelf.LIST_OF_GOODS_ON_SHELF.get(goodsName); // value for Map chosenGoods
            shoppingList.put(goodsName, price); // add chosen goods into private shopping list
            putGoodsToBacket(goodsName, price); // call the method which puts goods into the basket
        }
        System.out.println(this + "finished to choose goods");
        basket.setBasketList(shoppingList);
    }

    @Override
    public void putGoodsToBacket(String goodsName, double price) {
        sleepMethod(500, 2000, pensioner);
        System.out.println(this + "put " + goodsName + " to the basket, price for it is " + price + " BYN");
    }

    @Override
    public void goToQueue() {

//        Cashier cashier = QueueCashier.getStaff();
//        if (cashier != null) {
//            synchronized (cashier) {
//                cashier.notify();
//            }
//        }
/*
            try {
                Cashier.EXCHANGER.exchange(null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
*/

        synchronized (this) {
            QueueBuyers.add(this);
            waitSatet = true;
            while (waitSatet) {
                try {
                    if (this.isPensioner()) {
                        System.out.println(this + " added to queue for Pensioners");
                    } else {
                        System.out.println(this + " added to queue");
                    }
                    this.wait();
                    System.out.println(this + " left the queue");
                } catch (InterruptedException e) {
                    throw new RuntimeException("InterruptedException" + Thread.currentThread(), e);
                }
            }
        }
    }

    @Override
    public void goOut() {
        System.out.println(this + "left the basket");
        System.out.println(this + "left the shop");
        Manager.customerComeOut();
        Manager.closeTheShop();
    }

    @Override
    public String toString() {
        return getName();
    }


}
