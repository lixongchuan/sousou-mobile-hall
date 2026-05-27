package com.sousou.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sousou.demo.entity.MusicDTO;
import com.sousou.demo.service.MusicService;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    @GetMapping("/list")
    public Map<String, Object> getMusicList() {
        List<MusicDTO> list = musicService.loadFlacMusicList();
        Map<String, Object> res = new HashMap<>();
        res.put("code", 200);
        res.put("data", list);
        return res;
    }
}