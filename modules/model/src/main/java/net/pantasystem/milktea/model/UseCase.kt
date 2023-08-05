package net.pantasystem.milktea.model

interface UseCase

interface UseCase0<T> : UseCase {
    suspend operator fun invoke(): Result<T>
}

interface UseCase1<in P1, T> : UseCase {
    suspend operator fun invoke(p1: P1): Result<T>
}

interface UseCase2<in P1, in P2, T> : UseCase {
    suspend operator fun invoke(p1: P1, p2: P2): Result<T>
}

interface UseCase3<in P1, in P2, in P3, T> : UseCase {
    suspend operator fun invoke(p1: P1, p2: P2, p3: P3): Result<T>
}

interface UseCase4<in P1, in P2, in P3, in P4, T> : UseCase {
    suspend operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4): Result<T>
}

interface UseCase5<in P1, in P2, in P3, in P4, in P5, T> : UseCase {
    suspend operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5): Result<T>
}

interface UseCase6<in P1, in P2, in P3, in P4, in P5, in P6, T> : UseCase {
    suspend operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6): Result<T>
}

interface UseCase7<in P1, in P2, in P3, in P4, in P5, in P6, in P7, T> : UseCase {
    suspend operator fun invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7): Result<T>
}