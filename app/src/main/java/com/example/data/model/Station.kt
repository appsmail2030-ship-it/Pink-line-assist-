package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class Station(
    @PrimaryKey val id: String,
    val name: String,
    val sequenceNumber: Int,
    val direction: String = "CIRCULAR",
    val circularDirection: String = "Platform 1 (+ Circular Line)",
    val platform: String = "Platform 1 (+ Circular) & 2 (- Circular)",
    val isActive: Boolean = true
) {
    companion object {
        val DEFAULT_PINK_LINE_STATIONS = listOf(
            Station("ST_01", "Majlis Park", 1, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_02", "Azadpur", 2, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_03", "Shalimar Bagh", 3, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_04", "Netaji Subhash Place", 4, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_05", "Shakurpur", 5, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_06", "Punjabi Bagh West", 6, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_07", "ESI Hospital", 7, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_08", "Rajouri Garden", 8, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_09", "Maya Puri", 9, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_10", "Naraina Vihar", 10, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_11", "Delhi Cantt", 11, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_12", "Durgabai Deshmukh South Campus", 12, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_13", "Sir Vishweshwaraiah Moti Bagh", 13, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_14", "Bhikaji Cama Place", 14, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_15", "Sarojini Nagar", 15, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_16", "Dilli Haat - INA", 16, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_17", "South Extension", 17, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_18", "Lajpat Nagar", 18, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_19", "Vinobapuri", 19, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_20", "Ashram", 20, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_21", "Sarai Kale Khan - Hazrat Nizamuddin", 21, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_22", "Mayur Vihar - I", 22, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_23", "Mayur Vihar Pocket 1", 23, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_24", "Trilokpuri Sanjay Lake", 24, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_25", "East Vinod Nagar - Mayur Vihar II", 25, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_26", "Mandawali - West Vinod Nagar", 26, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_27", "IP Extension", 27, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_28", "Anand Vihar ISBT", 28, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_29", "Karkarduma", 29, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_30", "Karkarduma Court", 30, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_31", "Krishna Nagar", 31, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_32", "East Azad Nagar", 32, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_33", "Welcome", 33, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_34", "Jafrabad", 34, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_35", "Maujpur - Babarpur", 35, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_36", "Gokulpuri", 36, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_37", "Johri Enclave", 37, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true),
            Station("ST_38", "Shiv Vihar", 38, "CIRCULAR", "Platform 1 (+ Circular Line)", "Platform 1 (+ Circular) & 2 (- Circular)", true)
        )
    }
}
