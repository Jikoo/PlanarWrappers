package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarwrappers.mock.BukkitServer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Feature: Simplified hook for Vault softdependence")
@TestInstance(Lifecycle.PER_CLASS)
class VaultEconomyTest {

  Plugin plugin;
  VaultEconomy econHook;
  OfflinePlayer player;

  @BeforeEach
  void beforeEach() {
    Server server = BukkitServer.newServer();
    ServicesManager services = new SimpleServicesManager();
    doReturn(services).when(server).getServicesManager();
    PluginManager manager = new SimplePluginManager(server, new SimpleCommandMap(server));
    doReturn(manager).when(server).getPluginManager();

    plugin = mock(Plugin.class);
    doReturn(server).when(plugin).getServer();
    doReturn("VaultDependent").when(plugin).getName();
    Logger logger = mock(Logger.class);
    doReturn(logger).when(plugin).getLogger();
    econHook = new VaultEconomy(plugin);
    player = mock(OfflinePlayer.class);
    doReturn(new UUID(0, 0)).when(player).getUniqueId();
    doReturn("Player").when(player).getName();

    Bukkit.setServer(server);
  }

  @AfterEach
  void afterEach() {
    BukkitServer.unsetBukkitServer();
  }

  @Test
  void isPresentNoProvider() {
    assertThat("Null service is not present", econHook.isPresent(), is(false));
  }

  @Test
  void isPresentNotEnabled() {
    Economy economy = mockEcon();
    when(economy.isEnabled()).thenReturn(false);
    plugin.getServer().getServicesManager().register(Economy.class, economy, plugin, ServicePriority.Normal);
    assertThat("Disabled economy is not present", econHook.isPresent(), is(false));
  }

  @Test
  void isPresent() {
    registerEcon();
    assertThat("Enabled economy is present", econHook.isPresent());
  }

  @DisplayName("Logging is performed by default")
  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class Logging {

    // Separate localized economy instance that does not disable logging.
    VaultEconomy loggingEconHook;

    @BeforeAll
    void beforeAll() {
      loggingEconHook = new VaultEconomy(plugin);
    }

    @Test
    void logServiceClassNotLoaded() {
      assertThat(
          "Logging is present by default",
          loggingEconHook.logServiceClassNotLoaded(),
          is(notNullValue()));
    }

    @Test
    void logNoProviderRegistered() {
      assertThat(
          "Logging is present by default",
          loggingEconHook.logNoProviderRegistered(Economy.class),
          is(notNullValue()));
    }

    @Test
    void logServiceProviderChange() {
      assertThat(
          "Logging is present by default",
          loggingEconHook.logServiceProviderChange(Economy.class, mockEcon()),
          is(notNullValue()));
    }

  }

  @Test
  void getNameNoProvider() {
    assertThat("Response is sane default", econHook.getName(), is("null"));
  }

  @Test
  void getName() {
    registerEcon();
    assertThat("Method is handled by economy", econHook.getName(), is("MockEconomy"));
  }

  @Test
  void getFractionalDigitsNoProvider() {
    assertThat("Response is sane default", econHook.getFractionalDigits(), is(-1));
  }

  @Test
  void getFractionalDigits() {
    int value = 10;
    when(registerEcon().fractionalDigits()).thenReturn(value);
    assertThat(
        "Method is handled by economy",
        econHook.getFractionalDigits(),
        is(value));
  }

  @Test
  void formatNoProvider() {
    double value = 1000D;
    assertThat("Response is sane default", econHook.format(value), is(String.valueOf(value)));
  }

  @Test
  void format() {
    double amount = 1000;
    String value = "one thousand";
    when(registerEcon().format(amount)).thenReturn(value);
    assertThat("Method is handled by economy", econHook.format(amount), is(value));
  }

  @Test
  void getCurrencyNamePluralNoProvider() {
    assertThat("Response is sane default", econHook.getCurrencyNamePlural(), is(""));
  }

  @Test
  void getCurrencyNamePlural() {
    String value = "units";
    when(registerEcon().currencyNamePlural()).thenReturn(value);
    assertThat("Method is handled by economy", econHook.getCurrencyNamePlural(), is(value));
  }

  @Test
  void getCurrencyNameSingularNoProvider() {
    assertThat("Response is sane default", econHook.getCurrencyNameSingular(), is(""));
  }

  @Test
  void getCurrencyNameSingular() {
    String value = "absolute unit";
    when(registerEcon().currencyNameSingular()).thenReturn(value);
    assertThat("Method is handled by economy", econHook.getCurrencyNameSingular(), is(value));
  }

  @Test
  void hasPlayerAccountNoProvider() {
    assertThat("Response is sane default", econHook.hasPlayerAccount(player, null), is(false));
  }

  @Test
  void hasPlayerAccountNullWorld() {
    boolean value = true;
    when(registerEcon().hasAccount(player)).thenReturn(value);
    assertThat("Method is handled by economy", econHook.hasPlayerAccount(player, null), is(value));
  }

  @Test
  void hasPlayerAccount() {
    String world = "world";
    boolean value = true;
    when(registerEcon().hasAccount(player, world)).thenReturn(value);
    assertThat(
        "Method is handled by economy",
        econHook.hasPlayerAccount(player, world),
        is(value));
  }

  @Test
  void createPlayerAccountNoProvider() {
    assertThat("Response is sane default", econHook.createPlayerAccount(player, null), is(false));
  }

  @Test
  void createPlayerAccountNullWorld() {
    boolean value = true;
    when(registerEcon().createPlayerAccount(player)).thenReturn(value);
    assertThat(
        "Method is handled by economy",
        econHook.createPlayerAccount(player, null),
        is(value));
  }

  @Test
  void createPlayerAccount() {
    String world = "world";
    boolean value = true;
    when(registerEcon().createPlayerAccount(player, world)).thenReturn(value);
    assertThat(
        "Method is handled by economy",
        econHook.createPlayerAccount(player, world),
        is(value));
  }

  @Test
  void getPlayerBalanceNoProvider() {
    assertThat("Response is sane default", econHook.getPlayerBalance(player, null), is(0D));
  }

  @Test
  void getPlayerBalanceNullWorld() {
    double value = 1000D;
    when(registerEcon().getBalance(player)).thenReturn(value);
    assertThat("Method is handled by economy", econHook.getPlayerBalance(player, null), is(value));
  }

  @Test
  void getPlayerBalance() {
    double value = 1000D;
    String world = "world";
    when(registerEcon().getBalance(player, world)).thenReturn(value);
    assertThat(
        "Method is handled by economy",
        econHook.getPlayerBalance(player, world),
        is(value));
  }

  @Test
  void hasPlayerBalanceNoProvider() {
    double amount = 1000D;
    assertThat(
        "Response is sane default",
        econHook.hasPlayerBalance(player, null, amount),
        is(false));
  }

  @Test
  void hasPlayerBalanceNegative() {
    double amount = -1000D;
    registerEcon();
    assertThat(
        "Negative balance check is always true",
        econHook.hasPlayerBalance(player, null, amount),
        is(true));
  }

  @Test
  void hasPlayerBalanceNullWorld() {
    double amount = 1000D;
    boolean value = true;
    when(registerEcon().has(player, amount)).thenReturn(value);
    assertThat(
        "Method is handled by economy",
        econHook.hasPlayerBalance(player, null, amount),
        is(value));
  }

  @Test
  void hasPlayerBalance() {
    String world = "world";
    double amount = 1000;
    boolean value = true;
    when(registerEcon().has(player, world, amount)).thenReturn(value);
    assertThat(
        "Method is handled by economy",
        econHook.hasPlayerBalance(player, world, amount),
        is(value));
  }

  @Test
  void modifyPlayerBalanceNoProvider() {
    double amount = 1000D;
    assertThat(
        "Response is sane default",
        econHook.modifyPlayerBalance(player, null, amount),
        is(false));
  }

  @Test
  void modifyPlayerBalanceNoChange() {
    double amount = 0D;
    registerEcon();
    assertThat(
        "No change is a success",
        econHook.modifyPlayerBalance(player, null, amount),
        is(true));
  }

  @ParameterizedTest
  @MethodSource("getWorldBalanceMatrix")
  void modifyPlayerBalance(
      @Nullable String world,
      double amount,
      @NotNull Consumer<Economy> setup,
      boolean result) {
    setup.accept(registerEcon());
    assertThat(
        "Method is handled by economy",
        econHook.modifyPlayerBalance(player, world, amount),
        is(result));
  }

  public Collection<Arguments> getWorldBalanceMatrix() {
    Collection<Arguments> arguments = new ArrayList<>();

    for (String world : new String[] { "world", null }) {
      for (double amount : new double[] { 1000D, -1000D }) {
        for (boolean result : new boolean[] { true, false }) {

          Consumer<Economy> setup;
          ResponseType responseType = result ? ResponseType.SUCCESS : ResponseType.FAILURE;
          if (amount < 0) {
            EconomyResponse response = new EconomyResponse(amount, 0, responseType, null);
            if (world == null) {
              setup = economy -> when(economy.withdrawPlayer(player, -amount)).thenReturn(response);
            } else {
              setup = economy ->
                  when(economy.withdrawPlayer(player, world, -amount))
                      .thenReturn(response);
            }
          } else {
            EconomyResponse response = new EconomyResponse(amount, amount, responseType, null);
            if (world == null) {
              setup = economy -> when(economy.depositPlayer(player, amount)).thenReturn(response);
            } else {
              setup = economy ->
                  when(economy.depositPlayer(player, world, amount))
                      .thenReturn(response);
            }
          }

          arguments.add(Arguments.of(world, amount, setup, result));
        }
      }
    }

    return arguments;
  }

  @Test
  void hasBankSupportNoProvider() {
    assertThat("Response is sane default", econHook.hasBankSupport(), is(false));
  }

  @Test
  void hasBankSupport() {
    boolean value = true;
    when(registerEcon().hasBankSupport()).thenReturn(value);
    assertThat("Method is handled by economy", econHook.hasBankSupport(), is(value));
  }

  @Test
  void createBankNoProvider() {
    String bankName = "Server Bank"; // See, it's funny because...
    assertThat("Response is sane default", econHook.createBank(bankName, player), is(false));
  }

  @Test
  void createBank() {
    String bankName = "Server Bank";
    when(registerEcon().createBank(bankName, player))
        .thenReturn(new EconomyResponse(0, 0, ResponseType.SUCCESS, null));
    assertThat("Method is handled by economy", econHook.createBank(bankName, player), is(true));
  }

  @Test
  void deleteBankNoProvider() {
    String bankName = "Server Bank";
    assertThat("Response is sane default", econHook.deleteBank(bankName), is(false));
  }

  @Test
  void deleteBank() {
    String bankName = "Server Bank";
    when(registerEcon().deleteBank(bankName))
        .thenReturn(new EconomyResponse(0, 0, ResponseType.SUCCESS, null));
    assertThat("Method is handled by economy", econHook.deleteBank(bankName), is(true));
  }

  @Test
  void getBankBalanceNoProvider() {
    String bankName = "Server Bank";
    assertThat("Response is sane default", econHook.getBankBalance(bankName), is(0D));
  }

  @Test
  void getBankBalance() {
    String bankName = "Server Bank";
    double value = 1000D;
    when(registerEcon().bankBalance(bankName))
        .thenReturn(new EconomyResponse(0, value, ResponseType.SUCCESS, null));
    assertThat("Method is handled by economy", econHook.getBankBalance(bankName), is(value));
  }

  @Test
  void hasBankBalanceNoProvider() {
    String bankName = "Server Bank";
    double amount = 1000D;
    assertThat("Response is sane default", econHook.hasBankBalance(bankName, amount), is(false));
  }

  @Test
  void hasBankBalanceNegative() {
    String bankName = "Server Bank";
    double amount = -1000D;
    registerEcon();
    assertThat(
        "Negative balance check is always true",
        econHook.hasBankBalance(bankName, amount),
        is(true));
  }

  @Test
  void hasBankBalance() {
    String bankName = "Server Bank";
    double amount = 1000;
    boolean value = true;
    when(registerEcon().bankHas(bankName, amount))
        .thenReturn(new EconomyResponse(0, 1000, ResponseType.SUCCESS, null));
    assertThat(
        "Method is handled by economy",
        econHook.hasBankBalance(bankName, amount),
        is(value));
  }

  @Test
  void modifyBankBalanceNoProvider() {
    String bankName = "Server Bank";
    double amount = 1000D;
    assertThat(
        "Response is sane default",
        econHook.modifyBankBalance(bankName, amount),
        is(false));
  }

  @Test
  void modifyBankBalanceNoChange() {
    String bankName = "Server Bank";
    double amount = 0D;
    registerEcon();
    assertThat("No change is a success", econHook.modifyBankBalance(bankName, amount), is(true));
  }

  @ParameterizedTest
  @MethodSource("getBankBalanceMatrix")
  void modifyBankBalance(
      @NotNull String bankName,
      double amount,
      @NotNull Consumer<Economy> setup,
      boolean result) {
    setup.accept(registerEcon());
    assertThat(
        "Method is handled by economy",
        econHook.modifyBankBalance(bankName, amount),
        is(result));
  }

  public Collection<Arguments> getBankBalanceMatrix() {
    Collection<Arguments> arguments = new ArrayList<>();

    String bankName = "Server Bank";
    for (double amount : new double[] { 1000D, -1000D }) {
      for (boolean result : new boolean[] { true, false }) {
        Consumer<Economy> setup;
        ResponseType responseType = result ? ResponseType.SUCCESS : ResponseType.FAILURE;
        if (amount < 0) {
          EconomyResponse response = new EconomyResponse(amount, 0, responseType, null);
          setup = economy -> when(economy.bankWithdraw(bankName, -amount)).thenReturn(response);
        } else {
          EconomyResponse response = new EconomyResponse(amount, amount, responseType, null);
          setup = economy -> when(economy.bankDeposit(bankName, amount)).thenReturn(response);
        }

        arguments.add(Arguments.of(bankName, amount, setup, result));
      }
    }

    return arguments;
  }

  @Test
  void isBankOwnerNoProvider() {
    String bankName = "Server Bank";
    assertThat("Response is sane default", econHook.isBankOwner(bankName, player), is(false));
  }

  @Test
  void isBankOwner() {
    String bankName = "Server Bank";
    boolean value = true;
    when(registerEcon().isBankOwner(bankName, player))
        .thenReturn(new EconomyResponse(0, 0, ResponseType.SUCCESS, null));
    assertThat("Method is handled by economy", econHook.isBankOwner(bankName, player), is(value));
  }

  @Test
  void isBankMemberNoProvider() {
    String bankName = "Server Bank";
    assertThat("Response is sane default", econHook.isBankOwner(bankName, player), is(false));
  }

  @Test
  void isBankMember() {
    String bankName = "Server Bank";
    boolean value = true;
    when(registerEcon().isBankMember(bankName, player))
        .thenReturn(new EconomyResponse(0, 0, ResponseType.SUCCESS, null));
    assertThat("Method is handled by economy", econHook.isBankMember(bankName, player), is(value));
  }

  @Test
  void getBanksNoProvider() {
    assertThat("Response is sane default", econHook.getBanks(), is(empty()));
  }

  @Test
  void getBanks() {
    List<String> value = Arrays.asList("Cool", "Beans");
    Economy economy = registerEcon();
    when(economy.getBanks()).thenReturn(value);
    assertThat("Method is handled by economy", econHook.getBanks(), is(value));
  }

  private @NotNull Economy mockEcon() {
    Economy mockEconomy = mock(Economy.class);
    when(mockEconomy.getName()).thenReturn("MockEconomy");
    when(mockEconomy.isEnabled()).thenReturn(true);
    return mockEconomy;
  }

  private @NotNull Economy registerEcon() {
    Economy economy = mockEcon();
    plugin.getServer().getServicesManager()
        .register(Economy.class, economy, plugin, ServicePriority.Normal);
    return economy;
  }

}
