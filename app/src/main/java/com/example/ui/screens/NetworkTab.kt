package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LifeState
import com.example.data.model.MessageReply
import com.example.data.model.NpcMessage
import com.example.data.model.NpcRelation
import com.example.data.model.RelationRole
import com.example.engine.CascadeCommunicationEngine
import com.example.engine.NpcInteraction
import com.example.engine.formatCurrency
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun NetworkTab(
    life: LifeState,
    onInteract: (String, NpcInteraction) -> Unit,
    onMeetPerson: (Boolean) -> Unit,
    onReplyToMessage: (String, Int) -> Unit,
    onDirectDialogue: (String, MessageReply) -> Unit,
    modifier: Modifier = Modifier
) {
    val living = life.relations.filter { it.isAlive }
    val departed = life.relations.filterNot { it.isAlive }
    var dialogueTargetNpc by remember { mutableStateOf<NpcRelation?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onMeetPerson(false) },
                    enabled = life.isAlive && life.age >= 12,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("meet_friend_button")
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Meet Friend")
                }
                FilledTonalButton(
                    onClick = { onMeetPerson(true) },
                    enabled = life.isAlive && life.age >= 16,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("find_romance_button")
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = CrimsonLoss)
                    Spacer(Modifier.width(6.dp))
                    Text("Find Romance")
                }
            }
        }

        // NPC Communications Inbox
        if (life.inbox.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = if (life.unreadInboxCount > 0) Icons.Filled.MarkEmailUnread else Icons.Filled.MarkEmailRead,
                        contentDescription = "Inbox",
                        tint = if (life.unreadInboxCount > 0) BurnishedGold else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (life.unreadInboxCount > 0) "Inbox (${life.unreadInboxCount} awaiting reply)" else "Inbox",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            items(life.inbox, key = { it.id }) { msg ->
                NpcMessageCard(
                    message = msg,
                    life = life,
                    onReply = { replyIdx -> onReplyToMessage(msg.id, replyIdx) }
                )
            }
        }

        item {
            Text(
                "Inner Circle (${living.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (living.isEmpty()) {
            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        "No active close ties yet. Use Meet Friend or Find Romance above to build your inner circle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(living, key = { it.id }) { npc ->
                NpcCard(
                    npc = npc,
                    lifeAge = life.age,
                    isAlive = life.isAlive,
                    onInteract = { onInteract(npc.id, it) },
                    onOpenDialogue = { dialogueTargetNpc = npc }
                )
            }
        }

        if (departed.isNotEmpty()) {
            item {
                Text(
                    "In Memory",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(departed, key = { it.id }) { npc ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${npc.name} (${npc.role.label})", style = MaterialTheme.typography.bodyMedium)
                        Text("Passed at ${npc.age}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    val targetNpc = dialogueTargetNpc
    if (targetNpc != null) {
        val replies = remember(targetNpc.id, life.age, life.skills) {
            CascadeCommunicationEngine.buildDirectConversationReplies(targetNpc, life)
        }
        AlertDialog(
            onDismissRequest = { dialogueTargetNpc = null },
            title = {
                Column {
                    Text("Converse with ${targetNpc.name}", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "${targetNpc.role.label} • ${targetNpc.archetype.label} • Trust ${targetNpc.trust}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (targetNpc.lastConversationSummary.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                "Last memory: ${targetNpc.lastConversationSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    replies.forEachIndexed { idx, reply ->
                        val skillOk = reply.requiredSkill == null || life.skillLevel(reply.requiredSkill) >= reply.requiredLevel
                        OutlinedCard(
                            onClick = {
                                if (skillOk) {
                                    onDirectDialogue(targetNpc.id, reply)
                                    dialogueTargetNpc = null
                                }
                            },
                            enabled = skillOk,
                            border = BorderStroke(
                                1.dp,
                                if (skillOk) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialogue_option_$idx")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(reply.label, style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    reply.toneTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (skillOk) MaterialTheme.colorScheme.primary else CrimsonLoss
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogueTargetNpc = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun NpcMessageCard(
    message: NpcMessage,
    life: LifeState,
    onReply: (Int) -> Unit
) {
    val isPending = !message.isRead || (message.requiresReply && message.resolvedReplyText == null)
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isPending) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ),
        border = BorderStroke(
            width = if (isPending) 1.5.dp else 1.dp,
            color = if (isPending) BurnishedGold else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.testTag("inbox_message_${message.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(message.senderName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            message.senderRole,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    if (isPending) "AGE ${message.ageSent} • UNREAD" else "AGE ${message.ageSent} • REPLIED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPending) BurnishedGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(message.subject, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                message.body,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))
            if (message.resolvedReplyText != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGain.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, EmeraldGain.copy(alpha = 0.4f))
                ) {
                    Text(
                        "Outcome: ${message.resolvedReplyText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    message.replyOptions.forEachIndexed { idx, option ->
                        val hasCash = option.cashDelta >= 0 || life.cash >= abs(option.cashDelta)
                        val hasSkill = option.requiredSkill == null || life.skillLevel(option.requiredSkill) >= option.requiredLevel
                        val enabled = life.isAlive && hasCash && hasSkill

                        OutlinedButton(
                            onClick = { onReply(idx) },
                            enabled = enabled,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("inbox_reply_${message.id}_$idx")
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(option.label, style = MaterialTheme.typography.labelLarge)
                                val gateHint = when {
                                    !hasCash -> " • Need ${formatCurrency(abs(option.cashDelta))}"
                                    !hasSkill -> " • Locked (${option.requiredSkill?.label} Lv.${option.requiredLevel})"
                                    else -> ""
                                }
                                Text(
                                    option.toneTag + gateHint,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (enabled) MaterialTheme.colorScheme.primary else CrimsonLoss
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NpcCard(
    npc: NpcRelation,
    lifeAge: Int,
    isAlive: Boolean,
    onInteract: (NpcInteraction) -> Unit,
    onOpenDialogue: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(npc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                npc.role.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        "Age ${npc.age} • ${npc.occupation} • ${npc.archetype.label} • Est. ${formatCurrency(npc.netWorth)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isAlive) {
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = onOpenDialogue,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("converse_${npc.id}")
                    ) {
                        Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Converse with ${npc.name}", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Converse")
                    }
                }
            }

            if (npc.lastConversationSummary.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "\"${npc.lastConversationSummary}\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Closeness", style = MaterialTheme.typography.labelMedium)
                        Text("${npc.closeness}%", style = MaterialTheme.typography.labelSmall)
                    }
                    LinearProgressIndicator(
                        progress = { npc.closeness / 100f },
                        color = DeepTealAccent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Trust", style = MaterialTheme.typography.labelMedium)
                        Text("${npc.trust}%", style = MaterialTheme.typography.labelSmall)
                    }
                    LinearProgressIndicator(
                        progress = { npc.trust / 100f },
                        color = BurnishedGold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }

            if (isAlive) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(onClick = { onInteract(NpcInteraction.DEEP_TALK) }, label = { Text("Talk") })
                    AssistChip(onClick = { onInteract(NpcInteraction.QUALITY_TIME) }, label = { Text("Hang Out ($80)") })
                    AssistChip(onClick = { onInteract(NpcInteraction.GIFT) }, label = { Text("Gift ($500)") })
                    if (npc.role == RelationRole.PARTNER && lifeAge >= 18) {
                        AssistChip(onClick = { onInteract(NpcInteraction.PROPOSE) }, label = { Text("Propose ($2.5k)") })
                    }
                    if ((npc.role == RelationRole.PARTNER || npc.role == RelationRole.SPOUSE) && lifeAge >= 18) {
                        AssistChip(onClick = { onInteract(NpcInteraction.HAVE_CHILD) }, label = { Text("Have Child") })
                    }
                    if (npc.role == RelationRole.CHILD && npc.age >= 6) {
                        AssistChip(onClick = { onInteract(NpcInteraction.MENTOR) }, label = { Text("Mentor ($2k)") })
                    }
                    AssistChip(onClick = { onInteract(NpcInteraction.CUT_TIES) }, label = { Text("Cut Ties", color = CrimsonLoss) })
                }
            }
        }
    }
}
