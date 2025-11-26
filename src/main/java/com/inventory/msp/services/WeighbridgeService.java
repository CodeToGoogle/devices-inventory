package com.inventory.msp.services;

import com.inventory.msp.dto.WeighbridgeApiDTO;
import com.inventory.msp.model.WeighBridgeEntry;
import com.inventory.msp.model.WeighBridgeKey;
import com.inventory.msp.repository.WeighBridgeRepository;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;



@Service
public class WeighbridgeService {

    private static final Logger log= (Logger) LoggerFactory.getLogger(WeighbridgeService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String EXTERNAL_API = "http://103.133.215.48/api/ecostan";

    private final WeighBridgeRepository repo;

    public WeighbridgeService(WeighBridgeRepository repo) {
        this.repo = repo;
    }

    public void syncWeighbridgeData() {

        WeighbridgeApiDTO[] data =
                restTemplate.getForObject(EXTERNAL_API, WeighbridgeApiDTO[].class);

        if (data == null){
            log.info("No data found from external api WeighBridge Service, Dumping site: {}");
        }

        for (WeighbridgeApiDTO d : data) {

            WeighBridgeKey key = new WeighBridgeKey();
            key.setSlipno(d.getSlipno());
            key.setWbId(d.getWb_id());

            WeighBridgeEntry entry = new WeighBridgeEntry();
            entry.setId(key);

            entry.setVno(d.getVno());
            entry.setVname(d.getVname());
            entry.setSname(d.getSname());
            entry.setTweight(d.getTweight());
            entry.setGweight(d.getGweight());
            entry.setGdate(d.getGdate());
            entry.setTdate(d.getTdate());
            entry.setNweight(d.getNweight());
            entry.setDriver(d.getDriver());
            entry.setEdate(d.getEdate());
            entry.setZone(d.getZone());
            entry.setMts(d.getMts());
            entry.setWard(d.getWard());

            repo.save(entry);
        }
    }
}
