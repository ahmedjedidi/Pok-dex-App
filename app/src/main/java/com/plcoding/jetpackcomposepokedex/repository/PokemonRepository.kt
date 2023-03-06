package com.plcoding.jetpackcomposepokedex.repository

import com.plcoding.jetpackcomposepokedex.data.remote.PokeApi
import com.plcoding.jetpackcomposepokedex.data.remote.response.Pokemon
import com.plcoding.jetpackcomposepokedex.data.remote.response.PokemonList
import com.plcoding.jetpackcomposepokedex.utils.Resource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class PokemonRepository @Inject constructor(
    private val api:PokeApi
) {
    suspend fun getPokemonList(limit:Int,offset:Int): Resource<PokemonList> {
        val response = try {
            api.getPokemonList(limit, offset)
        }catch (e:Exception){
            return Resource.Error("An unknown Error occured")
        }
        return Resource.Success(response)
    }

    suspend fun getPokemonInfo(pokemonName:String): Resource<Pokemon> {
        val response = try {
            api.getPokemon(pokemonName)
        }catch (e:Exception){
            return Resource.Error("An unknown Error occured")
        }
        return Resource.Success(response)
    }
}