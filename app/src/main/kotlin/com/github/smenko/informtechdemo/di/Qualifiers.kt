package com.github.smenko.informtechdemo.di

import javax.inject.Qualifier


interface RoomQualifier {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SQL

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class InMemory
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainImmediateDispatcher