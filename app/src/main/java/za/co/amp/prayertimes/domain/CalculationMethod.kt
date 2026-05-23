package za.co.amp.prayertimes.domain

import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.CalculationMethod as AdhanCalculationMethod

/**
 * Supported Islamic prayer time calculation methods.
 *
 * Each value maps to a corresponding Adhan-Java [CalculationParameters] set.
 */
enum class CalculationMethod {
    /** Muslim World League — used across Europe, Far East, and parts of America. */
    MUSLIM_WORLD_LEAGUE,

    /** Islamic Society of North America (ISNA) — used in North America. */
    ISNA,

    /** Egyptian General Authority of Survey — used in Africa, Syria, Lebanon, Malaysia. */
    EGYPTIAN,

    /** Umm Al-Qura University, Makkah — used in the Arabian Peninsula. */
    UMM_AL_QURA;

    /**
     * Returns the Adhan-Java [CalculationParameters] corresponding to this method.
     */
    fun toAdhanParameters(): CalculationParameters = when (this) {
        MUSLIM_WORLD_LEAGUE -> AdhanCalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters()
        ISNA                -> AdhanCalculationMethod.NORTH_AMERICA.getParameters()
        EGYPTIAN            -> AdhanCalculationMethod.EGYPTIAN.getParameters()
        UMM_AL_QURA         -> AdhanCalculationMethod.UMM_AL_QURA.getParameters()
    }
}
