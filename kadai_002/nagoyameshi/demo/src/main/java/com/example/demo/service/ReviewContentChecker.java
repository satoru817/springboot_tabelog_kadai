package com.example.demo.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

@RequiredArgsConstructor
@Service
@Slf4j
public class ReviewContentChecker {

    @PostConstruct
    public void init() {
        try {
            // You need to provide the path to your language profiles
            // These profiles should be in your resources folder
            String profileDirectory = "src/main/resources/static/language";
            DetectorFactory.loadProfile(profileDirectory);
        } catch (LangDetectException e) {
            log.error("Failed to initialize language detection: " + e.getMessage());
        }
    }


    private final List<String> bannedWordsJapanese = List.of(
            "まずい", "クソ", "不味い", "腐ってる", "汚い", "汚れ", "最悪", "バカ", "アホ", "嫌い",
            "臭い", "偽物", "不潔", "大味", "薄い", "不味すぎ", "冷たい", "べちゃべちゃ", "生ゴミ", "食べれたもんじゃない",
            "虫", "ゴキブリ", "ひどい", "詐欺", "暴力", "無理", "不衛生", "胃もたれ", "食中毒", "病気",
            "不愉快", "最低", "お粗末", "食べ物じゃない", "味がしない", "不快", "二度と来ない", "残飯", "食べ残し", "変な味",
            "まずすぎ", "口に合わない", "傷んでる", "偽物", "不味い味付け", "苦い", "油っこい", "食材が古い", "まったく美味しくない",
            "嫌いな味", "ひどいサービス", "マナーが悪い", "不味い料理", "ひどい味", "食材の質が低い", "不衛生な環境", "放置された",
            "スカスカ", "過剰", "不十分", "遅い", "待たされる", "接客が悪い", "マズすぎる", "サービスが最低", "スタッフが無愛想",
            "従業員の態度が悪い", "むかつく", "不快なサービス", "遅すぎる", "小汚い", "値段に見合ってない", "クレーム", "だめだめ"
    );

    private final List<String> bannedWordsEnglish = List.of(
            "disgusting", "horrible", "terrible", "gross", "rotten", "stale", "bad service", "rude", "unpleasant",
            "nasty", "disappointing", "cold", "bland", "inedible", "uncooked", "burnt", "spoiled", "undercooked",
            "overcooked", "expired", "gag", "dirty", "smelly", "unhygienic", "food poisoning", "worst", "low quality",
            "horrible taste", "horrible service", "waited too long", "no flavor", "no seasoning", "stale bread",
            "too salty", "too sweet", "too greasy", "too dry", "overpriced", "not fresh", "chewy", "tough", "hard to eat",
            "horrible food", "wrong order", "unappetizing", "uneaten", "leftover", "disgusting smell", "burnt food",
            "toxic", "unsafe", "food waste", "unappetizing appearance", "unprofessional", "cheap quality", "cheap food",
            "horrible atmosphere", "soggy", "sour", "greasy", "lousy", "bland food", "non edible", "unhealthy",
            "too fatty", "ill prepared", "hard to swallow", "nasty taste", "unpleasant meal", "messy", "unorganized",
            "disaster", "inappropriate", "uninviting", "overcooked meat", "lackluster", "unoriginal", "underseasoned",
            "unfriendly staff", "noisy", "awkward", "unwelcoming", "stale drink", "stale food", "poor quality"
    );

    private final List<String> bannedWordsFrench = List.of(
            "dégueulasse", "horrible", "mauvais", "nul", "pire", "immonde", "moche", "désagréable", "dégoutant",
            "garde-manger", "pourri", "ancien", "froid", "insipide", "incuisiné", "brûlé", "moisi", "rancie",
            "repoussant", "sales", "malpropre", "infect", "poison alimentaire", "manger ça", "inutile", "trop salé",
            "trop sucré", "trop gras", "trop sec", "trop cuit", "sous-cuit", "réchauffé", "pas frais", "viande dure",
            "mauvais goût", "mauvais service", "attente trop longue", "pas de saveur", "pas d'assaisonnement", "tarte froide",
            "goût horrible", "très mauvais", "culinaire médiocre", "rien à manger", "fruits pourris", "tomates en mauvaise état",
            "mauvais environnement", "service horrible", "attitude désagréable", "temps d'attente", "trop d'huile",
            "vieux pain", "mauvais plat", "dégoûtant", "quasi immangeable", "mauvais accueil", "serveur désagréable",
            "cuisine insalubre", "cuisine indésirable", "trop gras", "servi froid", "viande avariée", "soupe trop salée",
            "sensation de faim", "mauvais rapport qualité-prix", "trop épicé", "trop sec", "trop lourd", "vieux produit",
            "mauvais restaurant", "pire restaurant", "très mauvais service", "absence de goût", "trop de sucre", "viande avariée"
    );

    public boolean containsBannedWordsJapanese(String content){
        return bannedWordsJapanese.stream().anyMatch(content::contains);
    }

    public boolean containsBannedWordsEnglish(String content){
        return bannedWordsEnglish.stream().anyMatch(word->content.contains(word));
    }

    public boolean containsBannedWordsFrench(String content){
        return bannedWordsFrench.stream().anyMatch(content::contains);
    }

    // 言語判定メソッド
    private String detectLanguage(String content) {
        try {
            if (content == null || content.trim().isEmpty()) {
                return null;
            }
            Detector detector = DetectorFactory.create();
            detector.append(content);
            return detector.detect(); // 例: "ja"（日本語）
        } catch (LangDetectException e) {
            // ログ出力などの処理を入れることも可能です
            System.err.println("言語検出に失敗しました: " + e.getMessage());
            return null;
        }
    }

    // 言語判定と禁止語句チェックの統合メソッド
    public boolean containsBannedWords(String content)  {
        String language = detectLanguage(content);
        log.info("language:{}",language);

        return switch (language) {
            case "ja" -> containsBannedWordsJapanese(content);
            case "en" -> containsBannedWordsEnglish(content);
            case "fr" -> containsBannedWordsFrench(content);
            default -> true;
        };
    }







}
