/*
 * BLOOM FILTER BASED FAKE NEWS DETECTION SYSTEM
 * Project: Fake News Detection using Bloom Filters
 * Purpose: Efficiently detect fake news articles using probabilistic data structures
 * 
 * Data Structure: Bloom Filter
 
 * Overall Time Complexity: O(k*n) where k = hash functions, n = elements

 */

import java.util.*;

/**
 * BloomFilterBitArrayResearch 
 */
class BloomFilterBitArrayResearch {
    
    private BitSet bitArray;
    private int bitArraySize;
    private final int NUM_HASH_FUNCTIONS = 3;
    private int expectedElements;
    private double targetFalsePositiveRate;
    
    /**
     * Constructor 
     * Time Complexity: O(m) where m is the calculated bit array size
     * Space Complexity: O(m)
     */
    public BloomFilterBitArrayResearch(int expectedElements, double targetFPR) {
        this.expectedElements = expectedElements;
        this.targetFalsePositiveRate = targetFPR;
        
        double ln2 = Math.log(2);
        this.bitArraySize = (int) Math.ceil(
            (-expectedElements * Math.log(targetFPR)) / (ln2 * ln2)
        );
        
        this.bitArray = new BitSet(bitArraySize);
        
        printInitialization();
    }
    
    /* 
     * Time Complexity: O(n) where n is the length of the key
     * Space Complexity: O(1)
     */
    public int hashMurmur(String key, int seed) {
        byte[] data = key.getBytes();
        final int m = 0x5bd1e995;
        final int r = 24;
        int h = seed ^ data.length;
        
        for (int i = 0; i < data.length; i++) {
            int k = data[i] & 0xff;
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }
        
        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;
        
        return Math.abs(h % bitArraySize);
    }
    
    /**
   
     * Time Complexity: O(n) where n is the length of the key
     * Space Complexity: O(1)
     */
    public int hashFNV1a(String key, int seed) {
        final long FNV_PRIME = 16777619L;
        final long FNV_OFFSET_BASIS = 2166136261L;
        
        long hash = FNV_OFFSET_BASIS ^ seed;
        byte[] data = key.getBytes();
        
        for (byte b : data) {
            hash ^= (b & 0xff);
            hash *= FNV_PRIME;
        }
        
        return Math.abs((int)(hash % bitArraySize));
    }
    
    /**
     * Time Complexity: O(n) where n is the length of the key
     * Space Complexity: O(1)
     */
    public int hashDJB2(String key, int seed) {
        long hash = 5381L + seed;
        byte[] data = key.getBytes();
        
        for (byte b : data) {
            hash = ((hash << 5) + hash) + (b & 0xff);
        }
        
        return Math.abs((int)(hash % bitArraySize));
    }
    
    /**
     * Set bit at specified position
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public void setBit(int position) {
        bitArray.set(position);
    }
    
    /**
     * Get bit value at specified position
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public boolean getBit(int position) {
        return bitArray.get(position);
    }
    
    /**
     * Clear bit at specified position
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public void clearBit(int position) {
        bitArray.clear(position);
    }
    
    /**
     * Get count of bits set to 1
     * Time Complexity: O(1) - BitSet maintains this internally
     * Space Complexity: O(1)
     */
    public int getBitsSetCount() {
        return bitArray.cardinality();
    }
    
    /**
     * Calculate fill rate percentage
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public double getFillRate() {
        return (double) bitArray.cardinality() / bitArraySize * 100;
    }
    
    /**
     * Get total bit array size
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public int getBitArraySize() {
        return bitArraySize;
    }  
    /**
     * Print initialization statistics
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    private void printInitialization() {
        System.out.println("Bloom Filter Initialized");
        System.out.println("Expected elements: " + expectedElements);
        System.out.println("Bit array size: " + bitArraySize + " bits");
        System.out.println("Hash functions: " + NUM_HASH_FUNCTIONS);
        System.out.println("Memory allocated: " + (bitArraySize / 8) + " bytes");
        System.out.println("Target FPR: " + String.format("%.2f%%", targetFalsePositiveRate * 100));
        System.out.println("Bits per element: " + String.format("%.2f", (double)bitArraySize/expectedElements));
        System.out.println();
    }
    
    /**
     * Print current filter statistics
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public void printStatistics() {
        System.out.println("\nBit Array Statistics");
        System.out.println("Total bits: " + bitArraySize);
        System.out.println("Bits set to 1: " + bitArray.cardinality());
        System.out.println("Bits set to 0: " + (bitArraySize - bitArray.cardinality()));
        System.out.println("Fill rate: " + String.format("%.2f%%", getFillRate()));
        System.out.println("Memory usage: " + (bitArraySize / 8) + " bytes");
    }

    /**
     * Time Complexity: O(k) where k = number of hash functions (3)
     * Space Complexity: O(1)
     */
    public void add(String item) {
        int[] positions = {
            hashMurmur(item, 0),
            hashFNV1a(item, 1),
            hashDJB2(item, 2)
        };
        for (int pos : positions) {
            bitArray.set(pos);
        }
    }

    /**
     * Time Complexity: O(k) where k = number of hash functions (3)
     * Space Complexity: O(1)
     
     */
    public boolean check(String item) {
        int[] positions = {
            hashMurmur(item, 0),
            hashFNV1a(item, 1),
            hashDJB2(item, 2)
        };
        for (int pos : positions) {
            if (!bitArray.get(pos)) {
                return false; // Definitely not present
            }
        }
        return true; // Possibly present (may be false positive)
    }
}

/**
 * FakeNewsDetector - Main detection engine using multiple Bloom filters
 * This class manages 9 specialized Bloom filters 
 * Overall Space Complexity: O(9m) where m is average filter size
 */
class FakeNewsDetector {
    private BloomFilterBitArrayResearch suspiciousWords;
    private BloomFilterBitArrayResearch clickbaitPhrases;
    private BloomFilterBitArrayResearch reliableSources;
    private BloomFilterBitArrayResearch fakeNewsDomains;
    private BloomFilterBitArrayResearch emotionalWords;
    private BloomFilterBitArrayResearch politicalBias;
    private BloomFilterBitArrayResearch medicalMisinformation;
    private BloomFilterBitArrayResearch verifiedAuthors;
    private BloomFilterBitArrayResearch satireIndicators;
    
    /**
     * Time Complexity: O(9 * m) where m is average bit array size
     * Space Complexity: O(9 * m)
     */
    public FakeNewsDetector() {
        System.out.println("=== INITIALIZING FAKE NEWS DETECTOR ===\n");
        
        System.out.println("1. Suspicious Words Filter:");
        suspiciousWords = new BloomFilterBitArrayResearch(500, 0.01);
        
        System.out.println("2. Clickbait Phrases Filter:");
        clickbaitPhrases = new BloomFilterBitArrayResearch(200, 0.01);
        
        System.out.println("3. Reliable Sources Filter:");
        reliableSources = new BloomFilterBitArrayResearch(100, 0.01);
        
        System.out.println("4. Known Fake News Domains Filter:");
        fakeNewsDomains = new BloomFilterBitArrayResearch(300, 0.01);
        
        System.out.println("5. Emotional Manipulation Filter:");
        emotionalWords = new BloomFilterBitArrayResearch(400, 0.01);
        
        System.out.println("6. Political Bias Filter:");
        politicalBias = new BloomFilterBitArrayResearch(350, 0.01);
        
        System.out.println("7. Medical Misinformation Filter:");
        medicalMisinformation = new BloomFilterBitArrayResearch(250, 0.01);
        
        System.out.println("8. Verified Authors Filter:");
        verifiedAuthors = new BloomFilterBitArrayResearch(150, 0.01);
        
        System.out.println("9. Satire Indicators Filter:");
        satireIndicators = new BloomFilterBitArrayResearch(100, 0.01);
        
        trainFilters();
    }
    
    /**
     * Time Complexity: O(n * k) where n = total training items, k = hash functions
     * Space Complexity: O(1) - only uses temporary arrays
     */
    private void trainFilters() {
        System.out.println("=== TRAINING FILTERS ===\n");
        
        String[] suspicious = {
            "shocking", "unbelievable", "secret", "exposed", "conspiracy",
            "they don't want you to know", "miracle cure", "doctors hate",
            "breaking", "urgent", "must see", "viral", "leaked", "censored",
            "banned", "hidden truth", "mainstream media", "wake up", "hoax",
            "scam", "lie", "coverup", "illuminati", "deep state"
        };
        
        String[] clickbait = {
            "you won't believe", "what happens next", "will shock you",
            "number 7 will", "this one trick", "they tried to hide",
            "mind blowing", "jaw dropping", "doctors hate him",
            "click here", "find out why", "the truth about",
            "what they don't tell you", "secrets revealed"
        };
        
        String[] reliable = {
            "reuters.com", "apnews.com", "bbc.com", "nytimes.com",
            "theguardian.com", "washingtonpost.com", "npr.org",
            "wsj.com", "bloomberg.com", "theatlantic.com"
        };
        
        String[] fakeDomains = {
            "fakenewssite.com", "conspiracy-today.net", "viral-stories.info",
            "click-bait-news.com", "shocking-revelations.org"
        };
        
        String[] emotional = {
            "outrage", "terrifying", "horrifying", "devastating", "panic",
            "fear", "angry", "furious", "disgusting", "alarming",
            "dangerous", "threat", "crisis", "disaster", "nightmare",
            "betrayal", "scandal", "corrupt", "evil", "hate"
        };
        
        String[] political = {
            "leftist agenda", "right-wing conspiracy", "liberal media",
            "conservative propaganda", "socialist plot", "fascist regime",
            "radical left", "extreme right", "fake news media",
            "biased reporting", "political correctness", "woke agenda"
        };
        
        String[] medical = {
            "miracle cure", "natural remedy cures", "big pharma hiding",
            "doctors don't want", "pharmaceutical conspiracy",
            "vaccine dangers", "alternative medicine proven",
            "toxins in vaccines", "chemical free cure",
            "herbs cure cancer", "superfood prevents"
        };
        
        String[] authors = {
            "anderson cooper", "christiane amanpour", "david muir",
            "lester holt", "norah o'donnell", "scott pelley",
            "margaret brennan", "jake tapper"
        };
        
        String[] satire = {
            "the onion", "satirical", "parody", "humor site",
            "comedy news", "fake news satire", "not real news"
        };
        
        System.out.println("Training all filters...");
        for (String word : suspicious) suspiciousWords.add(word.toLowerCase());
        for (String phrase : clickbait) clickbaitPhrases.add(phrase.toLowerCase());
        for (String source : reliable) reliableSources.add(source.toLowerCase());
        for (String domain : fakeDomains) fakeNewsDomains.add(domain.toLowerCase());
        for (String word : emotional) emotionalWords.add(word.toLowerCase());
        for (String phrase : political) politicalBias.add(phrase.toLowerCase());
        for (String phrase : medical) medicalMisinformation.add(phrase.toLowerCase());
        for (String author : authors) verifiedAuthors.add(author.toLowerCase());
        for (String indicator : satire) satireIndicators.add(indicator.toLowerCase());
        
        System.out.println("\n=== TRAINING COMPLETE ===\n");
    }
    
    /**
     * Comprehensive article analysis using all filters
     * Time Complexity: O(w * k) where w = word count, k = hash functions
     * Space Complexity: O(w) for storing words and results
     */
    public NewsAnalysis analyze(String headline, String content, String source, String author) {
        int suspiciousScore = 0;
        List<String> flags = new ArrayList<>();
        List<String> detectedPatterns = new ArrayList<>();
        
        String fullText = (headline + " " + content).toLowerCase();
        String[] words = fullText.split("\\s+");
        String[] sentences = fullText.split("\\.");
        
        System.out.println("\n=== ANALYZING ARTICLE ===");
        System.out.println("Headline: " + headline);
        System.out.println("Source: " + source);
        System.out.println("Author: " + author);
        System.out.println();
        
        // Check 1: Suspicious words - O(w * k)
        System.out.println("1. Checking for suspicious keywords...");
        int suspiciousCount = 0;
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
            if (word.length() > 3 && suspiciousWords.check(word)) {
                suspiciousCount++;
                detectedPatterns.add(word);
            }
        }
        
        if (suspiciousCount > 3) {
            suspiciousScore += 30;
            flags.add("Multiple suspicious keywords detected (" + suspiciousCount + " found)");
            System.out.println("   WARNING: Found " + suspiciousCount + " suspicious words");
        } else {
            System.out.println("   OK: Suspicious word check passed");
        }
        
        // Check 2: Clickbait - O(s * k) where s = sentence count
        System.out.println("2. Checking for clickbait patterns...");
        int clickbaitCount = 0;
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (clickbaitPhrases.check(sentence)) {
                clickbaitCount++;
            }
        }
        
        if (clickbaitCount > 0) {
            suspiciousScore += 25;
            flags.add("Clickbait phrases detected (" + clickbaitCount + " found)");
            System.out.println("   WARNING: Found " + clickbaitCount + " clickbait phrases");
        } else {
            System.out.println("   OK: Clickbait check passed");
        }
        
        // Check 3: Source reliability - O(k)
        System.out.println("3. Checking source reliability...");
        boolean isReliable = reliableSources.check(source.toLowerCase());
        boolean isKnownFake = fakeNewsDomains.check(source.toLowerCase());
        boolean isSatire = satireIndicators.check(source.toLowerCase());
        
        if (isSatire) {
            flags.add("SATIRE/PARODY SITE - Not meant as real news");
            System.out.println("   INFO: Satire/parody site detected");
        } else if (isKnownFake) {
            suspiciousScore += 40;
            flags.add("Source is a known fake news domain");
            System.out.println("   WARNING: Known fake news domain!");
        } else if (!isReliable) {
            suspiciousScore += 20;
            flags.add("Unknown or unverified source");
            System.out.println("   WARNING: Source not in reliable database");
        } else {
            System.out.println("   OK: Reliable source confirmed");
        }
        
        // Check 4: Emotional manipulation - O(w * k)
        System.out.println("4. Checking for emotional manipulation...");
        int emotionalCount = 0;
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            if (word.length() > 3 && emotionalWords.check(word)) {
                emotionalCount++;
            }
        }
        
        if (emotionalCount > 5) {
            suspiciousScore += 20;
            flags.add("Heavy emotional manipulation detected (" + emotionalCount + " emotional words)");
            System.out.println("   WARNING: High emotional manipulation: " + emotionalCount + " words");
        } else {
            System.out.println("   OK: Emotional manipulation check passed");
        }
        
        // Check 5: Political bias - O(s * k)
        System.out.println("5. Checking for political bias...");
        int biasCount = 0;
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.length() > 10 && politicalBias.check(sentence)) {
                biasCount++;
            }
        }
        
        if (biasCount > 0) {
            suspiciousScore += 15;
            flags.add("Political bias language detected");
            System.out.println("   WARNING: Political bias indicators found: " + biasCount);
        } else {
            System.out.println("   OK: Political bias check passed");
        }
        
        // Check 6: Medical misinformation - O(s * k)
        System.out.println("6. Checking for medical misinformation...");
        boolean hasMedicalMisinfo = false;
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (medicalMisinformation.check(sentence)) {
                hasMedicalMisinfo = true;
                break;
            }
        }
        
        if (hasMedicalMisinfo) {
            suspiciousScore += 35;
            flags.add("Potential medical misinformation detected");
            System.out.println("   WARNING: Medical misinformation patterns found!");
        } else {
            System.out.println("   OK: Medical misinformation check passed");
        }
        
        // Check 7: Author verification - O(k)
        System.out.println("7. Checking author credibility...");
        boolean isVerifiedAuthor = verifiedAuthors.check(author.toLowerCase());
        
        if (isVerifiedAuthor) {
            suspiciousScore -= 10;
            flags.add("Verified credible author");
            System.out.println("   OK: Verified credible author");
        } else {
            System.out.println("   INFO: Author not in verified database");
        }
        
        // Check 8: Excessive punctuation - O(n)
        System.out.println("8. Checking for sensationalism markers...");
        if (hasExcessivePunctuation(headline)) {
            suspiciousScore += 15;
            flags.add("Excessive punctuation (sensationalism indicator)");
            System.out.println("   WARNING: Excessive punctuation detected");
        } else {
            System.out.println("   OK: Punctuation check passed");
        }
        
        // Check 9: All caps - O(n)
        if (hasExcessiveCaps(headline)) {
            suspiciousScore += 10;
            flags.add("Excessive capitalization (attention-seeking)");
            System.out.println("   WARNING: Excessive capitalization detected");
        } else {
            System.out.println("   OK: Capitalization check passed");
        }
        
        // Check 10: URL structure - O(n)
        System.out.println("9. Checking URL structure...");
        if (hasSuspiciousURL(source)) {
            suspiciousScore += 10;
            flags.add("Suspicious URL structure detected");
            System.out.println("   WARNING: Suspicious URL pattern");
        } else {
            System.out.println("   OK: URL structure check passed");
        }
        
        suspiciousScore = Math.max(0, suspiciousScore);
        
        return new NewsAnalysis(suspiciousScore, flags, detectedPatterns);
    }
    
    /**
     * Check for excessive punctuation
     * Time Complexity: O(n) where n = text length
     * Space Complexity: O(1)
     */
    private boolean hasExcessivePunctuation(String text) {
        int punctCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '!' || c == '?') punctCount++;
        }
        return punctCount > 2;
    }
    
    /**
     * Check for excessive capitalization
     * Time Complexity: O(n) where n = text length
     * Space Complexity: O(1)
     */
    private boolean hasExcessiveCaps(String text) {
        if (text.length() < 10) return false;
        int capsCount = 0;
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) capsCount++;
        }
        return (capsCount * 1.0 / text.length()) > 0.5;
    }
    
    /**
     * Check for suspicious URL patterns
     * Time Complexity: O(n) where n = URL length
     * Space Complexity: O(1)
     */
    private boolean hasSuspiciousURL(String url) {
        url = url.toLowerCase();
        return url.contains("-news") || url.contains("real-") || 
               url.contains("true-") || url.contains(".co.") ||
               url.matches(".*\\d{4,}.*") || 
               url.split("\\.").length > 3;
    }
    
    /**
     * Detect fact-checking language
     * Time Complexity: O(n * p) where n = content length, p = number of phrases
     * Space Complexity: O(1)
     */
    public boolean hasFactCheckingLanguage(String content) {
        String[] factCheckPhrases = {
            "according to", "studies show", "research indicates",
            "data reveals", "statistics show", "peer reviewed"
        };
        
        content = content.toLowerCase();
        for (String phrase : factCheckPhrases) {
            if (content.contains(phrase)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculate content credibility score
     * Time Complexity: O(n) where n = content length
     * Space Complexity: O(1)
     */
    public int calculateCredibilityScore(String content) {
        int score = 50;
        
        if (content.contains("study") || content.contains("research")) score += 10;
        if (content.contains("according to")) score += 5;
        if (content.contains("expert") || content.contains("professor")) score += 5;
        
        if (content.contains("some say") || content.contains("many believe")) score -= 10;
        if (content.contains("allegedly") && content.split("allegedly").length > 3) score -= 5;
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Detect urgency manipulation tactics
     * Time Complexity: O(n * w) where n = headline length, w = number of urgency words
     * Space Complexity: O(1)
     */
    public boolean hasUrgencyManipulation(String headline) {
        String[] urgencyWords = {"urgent", "now", "immediately", "hurry", "limited time", "act fast"};
        headline = headline.toLowerCase();
        
        int urgencyCount = 0;
        for (String word : urgencyWords) {
            if (headline.contains(word)) urgencyCount++;
        }
        
        return urgencyCount >= 2;
    }
    
    /**
     * Check for proper sourcing and citations
     * Time Complexity: O(n) where n = content length
     * Space Complexity: O(1)
     */
    public boolean hasProperSourcing(String content) {
        return content.contains("source:") || 
               content.contains("reported by") ||
               content.contains("according to") ||
               content.matches(".*\\[\\d+\\].*");
    }
    
    /**
     * Print statistics for all Bloom filters
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public void printFilterStatistics() {
        System.out.println("\n=== BLOOM FILTER STATISTICS ===\n");
        
        System.out.println("1. Suspicious Words Filter:");
        suspiciousWords.printStatistics();
        
        System.out.println("\n2. Clickbait Phrases Filter:");
        clickbaitPhrases.printStatistics();
        
        System.out.println("\n3. Reliable Sources Filter:");
        reliableSources.printStatistics();
        
        System.out.println("\n4. Fake News Domains Filter:");
        fakeNewsDomains.printStatistics();
        
        System.out.println("\n5. Emotional Manipulation Filter:");
        emotionalWords.printStatistics();
        
        System.out.println("\n6. Political Bias Filter:");
        politicalBias.printStatistics();
        
        System.out.println("\n7. Medical Misinformation Filter:");
        medicalMisinformation.printStatistics();
        
        System.out.println("\n8. Verified Authors Filter:");
        verifiedAuthors.printStatistics();
        
        System.out.println("\n9. Satire Indicators Filter:");
        satireIndicators.printStatistics();
    }
}

/**
 * NewsAnalysis - Result container for analysis
 * Space Complexity: O(f) where f = number of flags
 */
class NewsAnalysis {
    private int score;
    private List<String> flags;
    private List<String> detectedPatterns;
    
    public NewsAnalysis(int score, List<String> flags, List<String> detectedPatterns) {
        this.score = score;
        this.flags = flags;
        this.detectedPatterns = detectedPatterns;
    }
    
    /**
     * Get  score
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public String getVerdict() {
        if (score >= 60) return "HIGH RISK - Likely Fake News";
        if (score >= 30) return "MEDIUM RISK - Verify Before Sharing";
        return "LOW RISK - Appears Credible";
    }
    
    /**
     * Get risk level 
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    public String getRiskLevel() {
        if (score >= 60) return "HIGH";
        if (score >= 30) return "MEDIUM";
        return "LOW";
    }
    
    /**
     * Generate  report
     * Time Complexity: O(f) where f = number of flags
     * Space Complexity: O(f)
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("     FAKE NEWS ANALYSIS RESULTS\n");
        sb.append("========================================\n\n");
        sb.append("Risk Score: ").append(score).append("/100\n");
        sb.append("Risk Level: ").append(getRiskLevel()).append("\n");
        sb.append("Verdict: ").append(getVerdict()).append("\n");
        sb.append("\n--- Flags Detected ---\n");
        if (flags.isEmpty()) {
            sb.append("  [OK] No major concerns detected\n");
        } else {
            for (int i = 0; i < flags.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(flags.get(i)).append("\n");
            }
        }
        
        if (!detectedPatterns.isEmpty() && detectedPatterns.size() <= 5) {
            sb.append("\n--- Detected Patterns (Sample) ---\n");
            for (int i = 0; i < Math.min(5, detectedPatterns.size()); i++) {
                sb.append("  * ").append(detectedPatterns.get(i)).append("\n");
            }
        }
        
        sb.append("\n");
        return sb.toString();
    }
}


public class BloomFilterFakeNewsDetector {
    
    /**
     * Main method
     * Time Complexity: O(n) where n = number of user interactions
     * Space Complexity: O(1) for the main loop, O(m) for detector initialization
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("===================================================");
        System.out.println("  BLOOM FILTER FAKE NEWS DETECTION SYSTEM");
        System.out.println("  Research-Grade Implementation");
        System.out.println("===================================================\n");
        
        FakeNewsDetector detector = new FakeNewsDetector();
        
        boolean continueAnalysis = true;
        
        while (continueAnalysis) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("CHOOSE AN OPTION:");
            System.out.println("=".repeat(60));
            System.out.println("1. Analyze your own article");
            System.out.println("2. Run demonstration tests");
            System.out.println("3. View Bloom filter statistics");
            System.out.println("4. Exit");
            System.out.print("\nEnter your choice (1-4): ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); 
            
            switch (choice) {
                case 1:
                    analyzeUserArticle(scanner, detector);
                    break;
                    
                case 2:
                    runDemonstrationTests(detector);
                    break;
                    
                case 3:
                    detector.printFilterStatistics();
                    break;
                    
                case 4:
                    continueAnalysis = false;
                    System.out.println("\n===================================================");
                    System.out.println("  Thank you for using Fake News Detector!");
                    System.out.println("===================================================");
                    break;
                    
                default:
                    System.out.println("\n[ERROR] Invalid choice. Please enter 1-4.");
            }
        }
        
        scanner.close();
    }
    
    /**
     * Analyzing user based articles
     * Time Complexity: O(w * k) where w = word count in article, k = hash functions
     * Space Complexity: O(w) for storing article content
     */
    private static void analyzeUserArticle(Scanner scanner, FakeNewsDetector detector) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("USER ARTICLE ANALYSIS");
        System.out.println("=".repeat(60));
        
        System.out.print("\nEnter article headline: ");
        String headline = scanner.nextLine();
        
        System.out.print("Enter article content: ");
        String content = scanner.nextLine();
        
        System.out.print("Enter source/website URL: ");
        String source = scanner.nextLine();
        
        System.out.print("Enter author name: ");
        String author = scanner.nextLine();
        
        // Analyze the article - O(w * k)
        NewsAnalysis result = detector.analyze(headline, content, source, author);
        System.out.println(result);
        
        // Additional analysis - O(n) for each check
        System.out.println("=== ADDITIONAL ANALYSIS ===");
        System.out.println("Fact-checking language present: " + 
            (detector.hasFactCheckingLanguage(content) ? "YES" : "NO"));
        System.out.println("Credibility score: " + 
            detector.calculateCredibilityScore(content) + "/100");
        System.out.println("Urgency manipulation: " + 
            (detector.hasUrgencyManipulation(headline) ? "DETECTED" : "NOT DETECTED"));
        System.out.println("Proper sourcing: " + 
            (detector.hasProperSourcing(content) ? "YES" : "NO"));
        
        System.out.println("\n[TIP] Always verify news from multiple trusted sources!");
    }
    
    /**
     * Running predefined news as test cases
     * Time Complexity: O(t * w * k) where t = number of tests, w = words per test
     * Space Complexity: O(w) for storing test content
     */
    private static void runDemonstrationTests(FakeNewsDetector detector) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RUNNING DEMONSTRATION TESTS");
        System.out.println("=".repeat(60));
        
        // Test case 1: Medical misinformation
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST CASE 1: SUSPICIOUS ARTICLE WITH MEDICAL CLAIMS");
        System.out.println("=".repeat(60));
        NewsAnalysis result1 = detector.analyze(
            "SHOCKING!!! Secret Miracle Cure Doctors Don't Want You To Know!!!",
            "You won't believe this amazing discovery that will shock you. " +
            "This one trick will change everything. The mainstream media tried to hide this truth. " +
            "Big pharma hiding the real cure. This alarming development will terrify you.",
            "fakenewssite.com",
            "Unknown Author"
        );
        System.out.println(result1);
        
        // Test case 2: real news
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST CASE 2: CREDIBLE ARTICLE");
        System.out.println("=".repeat(60));
        NewsAnalysis result2 = detector.analyze(
            "New Climate Report Released by International Panel",
            "Scientists present findings on global temperature trends and projections " +
            "for the next decade based on comprehensive research data. According to the study, " +
            "peer reviewed evidence shows significant climate changes.",
            "reuters.com",
            "Anderson Cooper"
        );
        System.out.println(result2);
        
        // Test case 3: Political bsed
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST CASE 3: POLITICALLY BIASED ARTICLE");
        System.out.println("=".repeat(60));
        NewsAnalysis result3 = detector.analyze(
            "Breaking: Viral video shows unbelievable incident!",
            "This shocking footage has taken the internet by storm. " +
            "You won't believe what happens next in this leaked video. " +
            "The leftist agenda is trying to suppress this information.",
            "unknown-blog.net",
            "Anonymous Blogger"
        );
        System.out.println(result3);
        
        // Test case 4: Satire detection
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST CASE 4: SATIRE ARTICLE");
        System.out.println("=".repeat(60));
        NewsAnalysis result4 = detector.analyze(
            "Local Man Discovers Meaning of Life in Fortune Cookie",
            "In a shocking turn of events, area resident finds philosophical enlightenment " +
            "at Chinese restaurant. This satirical story is meant for entertainment only.",
            "the onion",
            "Satire Writer"
        );
        System.out.println(result4);
        
        // Test case 5: Emotional manipulation
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST CASE 5: EMOTIONAL MANIPULATION");
        System.out.println("=".repeat(60));
        NewsAnalysis result5 = detector.analyze(
            "OUTRAGE! Terrifying Crisis Causes Panic and Fear!",
            "This horrifying and devastating disaster will make you furious. " +
            "The dangerous threat creates alarming nightmare scenario. " +
            "Disgusting betrayal and evil scandal exposed.",
            "viral-stories.info",
            "Clickbait Author"
        );
        System.out.println(result5);
        
        // Additional functions demo
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ADDITIONAL DETECTION FUNCTIONS");
        System.out.println("=".repeat(60));
        
        String testContent1 = "According to studies from Harvard, data reveals significant trends.";
        String testContent2 = "Some say this might be true. Many believe this is happening.";
        String testHeadline1 = "URGENT! Act Now! Limited Time! Hurry!";
        String testHeadline2 = "Research Published in Medical Journal";
        
        System.out.println("\nFact-Checking Language Detection:");
        System.out.println("Content 1: " + (detector.hasFactCheckingLanguage(testContent1) ? "[OK] Has fact-checking language" : "[NO] No fact-checking language"));
        System.out.println("Content 2: " + (detector.hasFactCheckingLanguage(testContent2) ? "[OK] Has fact-checking language" : "[NO] No fact-checking language"));
        
        System.out.println("\nCredibility Score:");
        System.out.println("Content 1 Score: " + detector.calculateCredibilityScore(testContent1) + "/100");
        System.out.println("Content 2 Score: " + detector.calculateCredibilityScore(testContent2) + "/100");
        
        System.out.println("\nUrgency Manipulation Detection:");
        System.out.println("Headline 1: " + (detector.hasUrgencyManipulation(testHeadline1) ? "[WARNING] Urgency manipulation detected" : "[OK] No urgency manipulation"));
        System.out.println("Headline 2: " + (detector.hasUrgencyManipulation(testHeadline2) ? "[WARNING] Urgency manipulation detected" : "[OK] No urgency manipulation"));
        
        System.out.println("\nSource Attribution Check:");
        System.out.println("Content 1: " + (detector.hasProperSourcing(testContent1) ? "[OK] Has proper sourcing" : "[NO] Missing proper sourcing"));
        System.out.println("Content 2: " + (detector.hasProperSourcing(testContent2) ? "[OK] Has proper sourcing" : "[NO] Missing proper sourcing"));
        
        System.out.println("\n===================================================");
        System.out.println("  DEMONSTRATION COMPLETE");
        System.out.println("  All 9 Bloom Filters Active");
        System.out.println("  10+ Detection Functions Implemented");
        System.out.println("===================================================");
    }
}
/*
 * 
 * Bloom Filters (9 total):
 * 1. Suspicious Words: ~4,793 bits (599 bytes)
 * 2. Clickbait Phrases: ~1,917 bits (240 bytes)
 * 3. Reliable Sources: ~959 bits (120 bytes)
 * 4. Fake Domains: ~2,876 bits (360 bytes)
 * 5. Emotional Words: ~3,834 bits (479 bytes)
 * 6. Political Bias: ~3,355 bits (419 bytes)
 * 7. Medical Misinfo: ~2,396 bits (300 bytes)
 * 8. Verified Authors: ~1,438 bits (180 bytes)
 * 9. Satire Indicators: ~959 bits (120 bytes)
 * 
 * Total Memory: ~22,527 bits (~2.8 KB) - Extremely space efficient!
 * 
 *
 */