package com.service.batch.database.crawling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.database.common.jpa.CommonDateEntity;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Entity
@Table(name = "PLAYLIST_TB")
public class PlaylistEntity extends CommonDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "musicId", nullable = false)
    private Long musicId;

    @Column(name = "slct", nullable = false, length = 1)
    private String slct;

    @Column(name = "no", nullable = false)
    private Long no;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "singer", nullable = false, length = 255)
    private String singer;

    @Column(name = "album", nullable = true, length = 255)
    private String album;

    @Column(name = "albumImg", nullable = true, columnDefinition = "TEXT")
    private String albumImg;

    @Column(name = "lyrics", nullable = true, columnDefinition = "TEXT")
    private String lyrics;

    @Column(name = "pubDate", nullable = true)
    private LocalDate pubDate;

    @Column(name = "youtubeLink", nullable = true, columnDefinition = "TEXT")
    private String youtubeLink;
}
