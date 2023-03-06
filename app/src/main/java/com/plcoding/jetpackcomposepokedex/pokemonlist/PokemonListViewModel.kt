package com.plcoding.jetpackcomposepokedex.pokemonlist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.plcoding.jetpackcomposepokedex.data.models.PokedexListEntry
import com.plcoding.jetpackcomposepokedex.repository.PokemonRepository
import com.plcoding.jetpackcomposepokedex.utils.Constants.PAGE_SIZE
import com.plcoding.jetpackcomposepokedex.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {
    private var curPage = 0

    var pokemonList = mutableStateOf<List<PokedexListEntry>>(listOf())
    var isLoading = mutableStateOf(false)
    var messageError = mutableStateOf("")
    var endReached = mutableStateOf(false)

    private var cachedPokemonList = listOf<PokedexListEntry>()
    private var isSearchStarting = true
    var isSearching = mutableStateOf(false)

    fun searchPokemonList(query:String){
        val listToSearch = if (isSearchStarting){
                pokemonList.value
        }else{
            cachedPokemonList
        }
        viewModelScope.launch(Dispatchers.Default) {
                if(query.isEmpty()){
                    pokemonList.value = cachedPokemonList
                    isSearching.value = false
                    isSearchStarting = true
                    return@launch
                }
        val result = listToSearch.filter {
            it.pokemonName.contains(query.trim(),ignoreCase = true) ||
                    it.number.toString() == query.trim()
        }
            if(isSearchStarting){
                cachedPokemonList = pokemonList.value
                isSearchStarting = true
            }
            pokemonList.value = result
            isSearching.value = true

        }

    }

    init {
        loadPokemonPaginated()
    }

    fun loadPokemonPaginated(){
        viewModelScope.launch {
            isLoading.value =true
            val result = pokemonRepository.getPokemonList(PAGE_SIZE, curPage * PAGE_SIZE)
            when(result){
                 is Resource.Success -> {
                     endReached.value = (curPage * PAGE_SIZE >= result.data!!.count)
                     val pokedexEntries = result.data.results.mapIndexed{ index, entry ->
                         val number = if(entry.url.endsWith("/")){
                             entry.url.dropLast(1).takeLastWhile { it.isDigit()}
                         }
                         else{
                             entry.url.takeLastWhile { it.isDigit() }
                         }
                         val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${number}.png"
                         PokedexListEntry(entry.name.replaceFirstChar {
                             if (it.isLowerCase()) it.titlecase(
                                 Locale.ROOT
                             ) else it.toString()
                         },url, number.toInt())


                     }
                     curPage++
                     isLoading.value=false
                     messageError.value=""
                     pokemonList.value+= pokedexEntries

                 }
                is Resource.Error -> {
                        isLoading.value = false
                        messageError.value = result.message!!
                }
            }
        }
    }



    fun calcDominantColor(drawable:Drawable, onFinish: (Color)->Unit){
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888,true)
        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }
}