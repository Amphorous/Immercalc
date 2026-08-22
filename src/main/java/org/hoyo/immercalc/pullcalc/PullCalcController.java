package org.hoyo.immercalc.pullcalc;

import org.hoyo.immercalc.pullcalc.model.GenshinEndDateRequest;
import org.hoyo.immercalc.pullcalc.model.GenshinEndDateResponse;
import org.hoyo.immercalc.pullcalc.model.GenshinPullCalcRequest;
import org.hoyo.immercalc.pullcalc.model.GenshinPullCalcResponse;
import org.hoyo.immercalc.pullcalc.service.GenshinPullCalcService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pullcalc")
public class PullCalcController {

    private final GenshinPullCalcService genshinPullCalcService;

    public PullCalcController(GenshinPullCalcService genshinPullCalcService) {
        this.genshinPullCalcService = genshinPullCalcService;
    }

    @PostMapping("/genshin/end-date")
    public ResponseEntity<GenshinEndDateResponse> genshinEndDate(@RequestBody GenshinEndDateRequest request) {
        return ResponseEntity.ok(genshinPullCalcService.calculateEndDate(request));
    }

    @PostMapping("/genshin/calculate")
    public ResponseEntity<GenshinPullCalcResponse> genshinCalculate(@RequestBody GenshinPullCalcRequest request) {
        return ResponseEntity.ok(genshinPullCalcService.calculate(request));
    }
}
