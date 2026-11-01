package kg.alatoo.sayakat.service;

import kg.alatoo.sayakat.entity.Provider;

public interface DatasetService {

    int importPois();

    int importLodgings(Provider owner);
}
