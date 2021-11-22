import { NativeModules } from 'react-native'

const { RNZendesk } = NativeModules

interface Config {
  appId: string
  clientId: string
  accountKey: string
  zendeskUrl: string
}

// // MARK: - Initialization

// export function initialize(config: Config) {
//   RNZendesk.initialize(config)
// }

// // MARK: - Indentification

// export function identifyJWT(token: string) {
//   RNZendesk.identifyJWT(token)
// }

// export function identifyAnonymous(name?: string, email?: string) {
//   RNZendesk.identifyAnonymous(name, email)
// }

// // MARK: - UI Methods

// interface HelpCenterOptions {
//   hideContactSupport?: boolean
// }

// export function showHelpCenter(options: HelpCenterOptions) {
//   RNZendesk.showHelpCenter(options)
// }

interface NewTicketOptions {
  tags?: string[],
  custom_fields?: string[]
}

// export function showNewTicket(options: NewTicketOptions) {
//   RNZendesk.showNewTicket(options)
// }

// export function showTicketList() {
//   RNZendesk.showTicketList()
// }

export default RNZendesk;