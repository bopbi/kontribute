package com.bobbyprabowo.kontribute.domain

import com.bobbyprabowo.kontribute.model.Contribution

class PairFinder {

    companion object {
        fun execute(contributions: List<List<List<Contribution>>>): List<Contribution> {
            val flattenContribution = contributions.flatMap { memberContribution ->
                memberContribution.flatten()
            }

            return flattenContribution.filter { contribution ->
                contribution.actors.size > 1
            }
        }
    }

}