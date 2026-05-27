package com.sousou.demo.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.sousou.demo.entity.LyricLine;
import com.sousou.demo.entity.MusicDTO;
import com.sousou.demo.exception.SousouException;

@Service
public class MusicService {

    public List<MusicDTO> loadFlacMusicList() {
        List<MusicDTO> list = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:/static/music/*.flac");

            int idIndex = 1;
            for (Resource resource : resources) {
                File file = resource.getFile();
                AudioFile audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTag();

                MusicDTO music = new MusicDTO();
                music.setId(idIndex++);
                
                String title = tag.getFirst(FieldKey.TITLE);
                String artist = tag.getFirst(FieldKey.ARTIST);
                music.setTitle(title.isEmpty() ? file.getName().replace(".flac", "") : title);
                music.setArtist(artist.isEmpty() ? "群星" : artist);
                
                music.setSrc("/music/" + file.getName());

                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] rawData = artwork.getBinaryData();
                    String base64Image = Base64.getEncoder().encodeToString(rawData);
                    music.setCoverBase64("data:" + artwork.getMimeType() + ";base64," + base64Image);
                } else {
                    music.setCoverBase64(""); 
                }

                String rawLyrics = tag.getFirst(FieldKey.LYRICS);
                music.setLyrics(parseLrcLyrics(rawLyrics));

                list.add(music);
            }
        } catch (Exception e) {
            throw new SousouException("读取 FLAC 音频媒体库失败，请确认 resources/static/music/ 文件夹下存在 FLAC 音乐。具体原因: " + e.getMessage());
        }
        return list;
    }

    private List<LyricLine> parseLrcLyrics(String rawLrc) {
        List<LyricLine> list = new ArrayList<>();
        if (rawLrc == null || rawLrc.trim().isEmpty()) {
            list.add(new LyricLine(0.0, "🎵 纯音乐，请欣赏 🎵"));
            return list;
        }

        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{1,3}))?\\](.*)");
        String[] lines = rawLrc.split("\\r?\\n");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                int min = Integer.parseInt(matcher.group(1));
                int sec = Integer.parseInt(matcher.group(2));
                int ms = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
                
                double absoluteTime = min * 60 + sec + (ms / 100.0);
                String text = matcher.group(4).trim();
                
                list.add(new LyricLine(absoluteTime, text));
            }
        }
        
        if (list.isEmpty()) {
            list.add(new LyricLine(0.0, "歌词未同步时间轴"));
        }
        return list;
    }
}