package org.robok.engine.ui.screens.settings.about

/*
 *  This file is part of Robok © 2024.
 *
 *  Robok is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Robok is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Robok.  If not, see <https://www.gnu.org/licenses/>.
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.androidx.compose.koinViewModel
import org.robok.engine.BuildConfig
import org.robok.engine.Drawables
import org.robok.engine.core.components.Screen
import org.robok.engine.core.components.preferences.base.PreferenceGroup
import org.robok.engine.defaults.DefaultContributors
import org.robok.engine.feature.settings.viewmodels.PreferencesViewModel
import org.robok.engine.models.about.Contributor
import org.robok.engine.models.about.Link
import org.robok.engine.strings.Strings
import org.robok.engine.ui.screens.settings.about.components.ContributorDialog
import org.robok.engine.ui.screens.settings.about.components.ContributorWidget
import org.robok.engine.ui.screens.settings.about.components.LinkWidget
import org.robok.engine.ui.screens.settings.about.viewmodel.AboutViewModel

var contributors = DefaultContributors()

@Composable
fun AboutScreen() {
  val appPrefsViewModel = koinViewModel<PreferencesViewModel>()
  val aboutViewModel = koinViewModel<AboutViewModel>()
  
  var contributorsState = rememberContributorsState()
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    contributors = fetchContributors()
    contributorsState.value = if (contributors.isEmpty()) DefaultContributors() else contributors
  }

  Screen(
    label = stringResource(id = Strings.settings_about_title),
    modifier = Modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) { innerPadding ->
    Column(
      modifier = Modifier.padding(top = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(id = Drawables.ic_launcher),
        contentDescription = null,
        modifier = Modifier.size(72.dp).clip(CircleShape),
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = stringResource(id = Strings.app_name),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge,
      )
      Text(
        text = BuildConfig.VERSION_NAME,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.requiredHeight(16.dp))
    }

    var viewModel.isShowContributorDialog by remember { mutableStateOf(false) }
    if (contributorsState.value.isNotEmpty()) {
      val roles = contributorsState.value.groupBy { it.role }
      roles.forEach { (role, contributorsList) ->
        PreferenceGroup(heading = role) {
          contributorsList.forEach {
            ContributorWidget(
              model = it,
              onClick = { contributor ->
                viewModel.setShowContributorDialog(true)
                viewModel.setCurrentContributor(contributor)
              },
            )
          }
        }
      }
    }

    PreferenceGroup(heading = stringResource(id = Strings.text_seeus)) {
      getLinksList().forEach { LinkWidget(model = it) }
    }
  }

  if (viewModel.isShowContributorDialog) {
    ContributorDialog(
      contributor = viewModel.currentContributor,
      onDismissRequest = { viewModel.setShowContributorDialog(false) },
    )
  }
}

@Composable private fun rememberContributorsState() = remember { mutableStateOf(contributors) }

private fun getLinksList(): List<Link> {
  return listOf(
    Link(
      name = stringResource(id = Strings.title_github),
      description = stringResource(id = Strings.text_github),
      imageResId = Drawables.ic_github_24,
      url = stringResource(id = Strings.link_github),
    ),
    Link(
      name = stringResource(id = Strings.title_telegram),
      description = stringResource(id = Strings.text_telegram),
      imageResId = Drawables.ic_send_24,
      url = stringResource(id = Strings.link_telegram),
    ),
    Link(
      name = stringResource(id = Strings.title_whatsapp),
      description = stringResource(id = Strings.text_whatsapp),
      imageResId = Drawables.ic_whatsapp_24,
      url = stringResource(id = Strings.link_whatsapp),
    ),
  )
}

val client = OkHttpClient()

suspend fun fetchContributors(): List<Contributor> {
  val request =
    Request.Builder()
      .url(
        "https://raw.githubusercontent.com/robok-inc/Robok-Engine/host/.github/contributors/contributors_github.json"
      )
      .build()

  return withContext(Dispatchers.IO) {
    try {
      client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
          val jsonString = response.body?.string()
          jsonString?.let {
            val contributors = Json.decodeFromString<List<Contributor>>(it)

            contributors.filter { contributor ->
              contributor.type != "Bot" &&
                contributor.role != "Bot" &&
                contributor.user_view_type != "private"
            }
          } ?: emptyList()
        } else {
          emptyList()
        }
      }
    } catch (e: Exception) {
      emptyList()
    }
  }
}
