package com.github.smenko.informtechdemo.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.github.smenko.informtechdemo.R
import com.github.smenko.informtechdemo.databinding.ActivityMainBinding
import com.github.smenko.informtechdemo.utils.SearchViewListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var searchActionAvailable = false
    var searchViewListener: SearchViewListener? = null
        set(value) {
            field = value
            invalidateOptionsMenu()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavView = binding.contentInclude.navView
        bottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            searchActionAvailable = destination.id == R.id.navigation_contacts
            invalidateOptionsMenu()
        }

        // Handle software keyboard hide/show
        KeyboardVisibilityEvent.setEventListener(this, this) {
            if (!it) {
                showBottomNav()
            } else {
                hideBottomNav()
            }
        }
    }

    private fun showBottomNav() {
        bottomNavView.animate().translationY(0f).start()
    }

    private fun hideBottomNav() {
        bottomNavView.animate().translationY(400f).start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar, menu)
        menu.findItem(R.id.app_bar_search).apply {
            isVisible = searchActionAvailable
            (actionView as SearchView?)?.apply {
                queryHint = resources.getString(R.string.search_view_hint)
                setOnSearchClickListener(searchViewListener)
                setOnQueryTextListener(searchViewListener)
                setOnCloseListener(searchViewListener)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.app_bar_settings -> {
                Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}